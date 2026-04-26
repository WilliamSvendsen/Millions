package no.ntnu.idatx2003.millions.model.save;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import no.ntnu.idatx2003.millions.model.Exchange;
import no.ntnu.idatx2003.millions.model.Player;
import no.ntnu.idatx2003.millions.model.Share;
import no.ntnu.idatx2003.millions.model.Stock;

/**
 * Service for saving and loading the game state to and from a JSON file.
 *
 * <p>The save file stores the player's name, starting money, current money,
 * all stocks with their full price history, and the player's current portfolio.</p>
 */
public class GameSaveService {

    // Gson instance configured to produce readable formatted JSON
    private final Gson gson;

    /**
     * Creates a new GameSaveService.
     */
    public GameSaveService() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Saves the current game state to a JSON file.
     *
     * @param player   the current player
     * @param exchange the current exchange
     * @param filePath path to save the file to
     * @throws IOException if the file cannot be written
     */
    public void saveGame(Player player, Exchange exchange, String filePath)
            throws IOException {
        JsonObject root = new JsonObject();

        // Save player data
        JsonObject playerData = new JsonObject();
        playerData.addProperty("name", player.getName());
        playerData.addProperty("startingMoney", player.getStartingMoney().toString());
        playerData.addProperty("money", player.getMoney().toString());
        root.add("player", playerData);

        // Save week number
        root.addProperty("week", exchange.getWeek());

        // Save all stocks with their full price history
        JsonArray stocksArray = new JsonArray();
        for (Stock stock : exchange.findStocks("")) {
            JsonObject stockData = new JsonObject();
            stockData.addProperty("symbol", stock.getSymbol());
            stockData.addProperty("company", stock.getCompany());

            JsonArray pricesArray = new JsonArray();
            stock.getHistoricalPrices().forEach(p -> pricesArray.add(p.toString()));
            stockData.add("prices", pricesArray);

            stocksArray.add(stockData);
        }
        root.add("stocks", stocksArray);

        // Save portfolio
        JsonArray portfolioArray = new JsonArray();
        for (Share share : player.getPortfolio().getShares()) {
            JsonObject shareData = new JsonObject();
            shareData.addProperty("symbol", share.getStock().getSymbol());
            shareData.addProperty("quantity", share.getQuantity().toString());
            shareData.addProperty("purchasePrice", share.getPurchasePrice().toString());
            portfolioArray.add(shareData);
        }
        root.add("portfolio", portfolioArray);

        // Write to file
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(gson.toJson(root));
        }
    }

    /**
     * Loads a game state from a JSON file.
     * Returns a GameState object containing the restored player and exchange.
     *
     * @param filePath path to the save file
     * @return restored GameState
     * @throws IOException if the file cannot be read
     */
    public GameState loadGame(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            // Restore stocks with full price history
            List<Stock> stocks = new ArrayList<>();
            JsonArray stocksArray = root.getAsJsonArray("stocks");
            for (var stockElement : stocksArray) {
                JsonObject stockData = stockElement.getAsJsonObject();
                String symbol = stockData.get("symbol").getAsString();
                String company = stockData.get("company").getAsString();

                JsonArray pricesArray = stockData.getAsJsonArray("prices");
                BigDecimal firstPrice = new BigDecimal(
                        pricesArray.get(0).getAsString());
                Stock stock = new Stock(symbol, company, firstPrice);

                // Add remaining prices to restore full history
                for (int i = 1; i < pricesArray.size(); i++) {
                    stock.addNewSalesPrice(
                            new BigDecimal(pricesArray.get(i).getAsString()));
                }
                stocks.add(stock);
            }

            // Restore exchange with correct week number
            Exchange exchange = new Exchange("Millions Exchange", stocks);
            int savedWeek = root.get("week").getAsInt();
            // Advance the internal week counter to match saved week
            for (int i = 1; i < savedWeek; i++) {
                exchange.advanceWeekCounter();
            }

            // Restore player
            JsonObject playerData = root.getAsJsonObject("player");
            String name = playerData.get("name").getAsString();
            BigDecimal startingMoney = new BigDecimal(
                    playerData.get("startingMoney").getAsString());
            BigDecimal currentMoney = new BigDecimal(
                    playerData.get("money").getAsString());

            Player player = new Player(name, startingMoney);
            // Adjust money to match saved amount
            if (currentMoney.compareTo(startingMoney) < 0) {
                player.withdrawMoney(startingMoney.subtract(currentMoney));
            } else if (currentMoney.compareTo(startingMoney) > 0) {
                player.addMoney(currentMoney.subtract(startingMoney));
            }

            // Restore portfolio
            JsonArray portfolioArray = root.getAsJsonArray("portfolio");
            for (var shareElement : portfolioArray) {
                JsonObject shareData = shareElement.getAsJsonObject();
                String symbol = shareData.get("symbol").getAsString();
                BigDecimal quantity = new BigDecimal(
                        shareData.get("quantity").getAsString());
                BigDecimal purchasePrice = new BigDecimal(
                        shareData.get("purchasePrice").getAsString());

                Stock stock = exchange.getStock(symbol);
                Share share = new Share(stock, quantity, purchasePrice);
                player.getPortfolio().addShare(share);
            }

            return new GameState(player, exchange);
        }
    }
}
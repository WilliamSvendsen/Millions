package no.ntnu.idatx2003.millions.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import no.ntnu.idatx2003.millions.model.Exchange;
import no.ntnu.idatx2003.millions.model.Player;
import no.ntnu.idatx2003.millions.model.Share;
import no.ntnu.idatx2003.millions.model.file.CsvStockFileHandler;
import no.ntnu.idatx2003.millions.model.file.StockFileHandler;
import no.ntnu.idatx2003.millions.model.save.GameSaveService;
import no.ntnu.idatx2003.millions.model.save.GameState;

/**
 * Controller for the Millions game.
 * Sits between the view and the model - handles all game actions
 * triggered by the user through the GUI.
 */
public class GameController {

    // The exchange is the central model object
    private Exchange exchange;

    // The player currently playing the game
    private Player player;

    // The file handler used to load stocks from CSV
    private final StockFileHandler fileHandler;

    // Service for saving and loading game state
    private final GameSaveService saveService;

    /**
     * Creates a new GameController with a CSV file handler.
     */
    public GameController() {
        this.fileHandler = new CsvStockFileHandler();
        this.saveService = new GameSaveService();
    }

    /**
     * Starts a new game by creating a player and loading stocks from a file.
     *
     * @param playerName    the name of the player
     * @param startingMoney the player's starting capital
     * @param filePath      path to the CSV file containing stock data
     * @throws IOException if the file cannot be read
     * @throws IllegalArgumentException if any argument is invalid
     */
    public void startNewGame(String playerName, BigDecimal startingMoney, String filePath)
            throws IOException {
        this.player = new Player(playerName, startingMoney);
        this.exchange = new Exchange("Millions Exchange", new ArrayList<>());
        exchange.loadStocksFromFile(filePath, fileHandler);
    }

    /**
     * Buys a stock for the current player and returns the completed transaction.
     *
     * @param symbol   the stock symbol to buy
     * @param quantity the number of units to buy
     * @return the completed transaction
     * @throws IllegalArgumentException if symbol is invalid, such as letters and non-positive numbers.
     * @throws IllegalStateException if the player doesn't have enough funds
     */
    public no.ntnu.idatx2003.millions.model.transaction.Transaction buyStock(
            String symbol, BigDecimal quantity) {
        return exchange.buy(symbol, quantity, player);
    }

    /**
     * Sells a share from the player's portfolio and returns the completed transaction.
     *
     * @param share the share to sell
     * @return the completed transaction
     * @throws IllegalStateException if the player own the share
     */
    public no.ntnu.idatx2003.millions.model.transaction.Transaction sellShare(Share share) {
        return exchange.sell(share, player);
    }

    /**
     * Advances the game to the next trading week.
     * Returns any random event messages that occurred.
     *
     * @return list of event messages
     */
    public List<String> advanceWeek() {
        return exchange.advance();
    }

    /**
     * Sells all shares in the player's portfolio.
     */
    public void sellAll() {
        List<Share> shares = new ArrayList<>(player.getPortfolio().getShares());
        shares.forEach(share -> exchange.sell(share, player));
    }

    /**
     * Returns the current exchange.
     *
     * @return exchange
     */
    public Exchange getExchange() {
        return exchange;
    }

    /**
     * Returns the current player.
     *
     * @return player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Saves the current game state to a JSON file.
     *
     * @param filePath path to save to
     * @throws IOException if the file cannot be written
     */
    public void saveGame(String filePath) throws IOException {
        saveService.saveGame(player, exchange, filePath);
    }

    /**
     * Loads a game state from a JSON file.
     *
     * @param filePath path to load from
     * @throws IOException if the file cannot be read
     */
    public void loadGame(String filePath) throws IOException {
        GameState state = saveService.loadGame(filePath);
        this.player = state.getPlayer();
        this.exchange = state.getExchange();
    }
}
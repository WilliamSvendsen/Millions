package no.ntnu.idatx2003.millions.view;

import java.math.BigDecimal;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import no.ntnu.idatx2003.millions.controller.GameController;
import no.ntnu.idatx2003.millions.model.Share;
import no.ntnu.idatx2003.millions.model.Stock;
import no.ntnu.idatx2003.millions.observer.Observer;

/**
 * The main game view. Displays stocks, portfolio, transactions
 * and player info. Implements Observer so it updates automatically
 * when the model changes.
 */
public class MainView implements Observer {

    private final Stage stage;
    private final GameController controller;

    // Player info labels updated in real time
    private Label moneyLabel;
    private Label netWorthLabel;
    private Label statusLabel;
    private Label weekLabel;

    // Stock list and search
    private ListView<String> stockListView;
    private TextField searchField;

    // Portfolio list
    private ListView<String> portfolioListView;

    // Transaction history list
    private ListView<String> transactionListView;

    // Gainers and losers lists
    private ListView<String> gainersListView;
    private ListView<String> losersListView;

    /**
     * Creates a new MainView.
     *
     * @param stage the primary stage
     * @param controller the game controller
     */
    public MainView(Stage stage, GameController controller) {
        this.stage = stage;
        this.controller = controller;
        // Register this view as an observer of the exchange
        controller.getExchange().addObserver(this);
    }

    /**
     * Builds and displays the main game window.
     */
    public void show() {
        moneyLabel = new Label();
        netWorthLabel = new Label();
        statusLabel = new Label();
        weekLabel = new Label();
        updatePlayerInfo();

        HBox topBar = new HBox(20, weekLabel, moneyLabel, netWorthLabel, statusLabel);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #2c2c2c;");
        topBar.setAlignment(Pos.CENTER_LEFT);
        for (var node : topBar.getChildren()) {
            if (node instanceof Label label) {
                label.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
            }
        }

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().addAll(
                buildStocksTab(),
                buildPortfolioTab(),
                buildTransactionsTab(),
                buildMarketTab()
        );

        Button advanceButton = new Button("Advance to Next Week");
        advanceButton.setStyle("-fx-font-size: 13px;");
        advanceButton.setOnAction(e -> {
            List<String> events = controller.advanceWeek();
            StringBuilder message = new StringBuilder(
                    "Advanced to week " + controller.getExchange().getWeek()
                            + ". Prices have been updated.");
            if (!events.isEmpty()) {
                message.append("\n\nMarket Events This Week:");
                events.forEach(event -> message.append("\n").append(event));
            }
            showInfo(message.toString());
        });

        Button sellAllButton = new Button("Sell All & Exit");
        sellAllButton.setStyle("-fx-font-size: 13px; -fx-text-fill: red;");
        sellAllButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to sell all shares and exit?",
                    ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    controller.sellAll();
                    showInfo("All shares sold! Final balance: "
                            + controller.getPlayer().getMoney());
                    stage.close();
                }
            });
        });

        Button saveButton = new Button("Save Game");
        saveButton.setStyle("-fx-font-size: 13px;");
        saveButton.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save Game");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("JSON files", "*.json"));
            fileChooser.setInitialFileName("millions_save.json");
            java.io.File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    controller.saveGame(file.getAbsolutePath());
                    showInfo("Game saved successfully!");
                } catch (java.io.IOException ex) {
                    showError("Could not save game: " + ex.getMessage());
                }
            }
        });

        Button loadButton = new Button("Load Game");
        loadButton.setStyle("-fx-font-size: 13px;");
        loadButton.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Load Game");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("JSON files", "*.json"));
            java.io.File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    controller.loadGame(file.getAbsolutePath());
                    controller.getExchange().addObserver(this);
                    update();
                    showInfo("Game loaded successfully!");
                } catch (java.io.IOException ex) {
                    showError("Could not load game: " + ex.getMessage());
                }
            }
        });

        HBox bottomBar = new HBox(10, advanceButton, saveButton, loadButton, sellAllButton);
        bottomBar.setPadding(new Insets(10));
        bottomBar.setAlignment(Pos.CENTER_RIGHT);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(tabPane);
        root.setBottom(bottomBar);

        Scene scene = new Scene(root, 900, 650);
        stage.setTitle("Millions - Week " + controller.getExchange().getWeek());
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Builds the Stocks tab where the player can browse and buy stocks.
     */
    private Tab buildStocksTab() {
        Tab tab = new Tab("Stocks");

        searchField = new TextField();
        searchField.setPromptText("Search by symbol or name...");
        searchField.textProperty().addListener((obs, old, newVal) -> refreshStockList());

        stockListView = new ListView<>();
        refreshStockList();

        Label quantityLabel = new Label("Quantity:");
        TextField quantityField = new TextField();
        quantityField.setPromptText("e.g. 5");
        quantityField.setPrefWidth(100);

        Button buyButton = new Button("Buy Selected");
        buyButton.setOnAction(e -> {
            String selected = stockListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Please select a stock to buy.");
                return;
            }
            String symbol = selected.split(" ")[0];
            try {
                BigDecimal quantity = new BigDecimal(quantityField.getText().trim());
                if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                    showError("Quantity must be positive.");
                    return;
                }

                // Show cost preview before confirming
                Stock stock = controller.getExchange().getStock(symbol);
                no.ntnu.idatx2003.millions.model.calculator.PurchaseCalculator preview =
                        new no.ntnu.idatx2003.millions.model.calculator.PurchaseCalculator(
                                new no.ntnu.idatx2003.millions.model.Share(
                                        stock, quantity, stock.getSalesPrice()));

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Purchase Preview"
                                + "\n--------------------------"
                                + "\nStock: " + symbol
                                + "\nQuantity: " + quantity
                                + "\nPrice per unit: " + stock.getSalesPrice()
                                + "\nGross: " + preview.calculateGross()
                                + "\nCommission (0.5%): " + preview.calculateCommission()
                                + "\nTotal Cost: " + preview.calculateTotal()
                                + "\n\nYour balance: " + controller.getPlayer().getMoney()
                                + "\nBalance after purchase: " + controller.getPlayer().getMoney()
                                .subtract(preview.calculateTotal())
                                + "\n\nConfirm purchase?",
                        ButtonType.YES, ButtonType.NO);

                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        try {
                            no.ntnu.idatx2003.millions.model.transaction.Transaction t =
                                    controller.buyStock(symbol, quantity);
                            showInfo("Purchase Receipt"
                                    + "\n--------------------------"
                                    + "\nStock: " + symbol
                                    + "\nQuantity: " + quantity
                                    + "\nGross: " + t.getCalculator().calculateGross()
                                    + "\nCommission: " + t.getCalculator().calculateCommission()
                                    + "\nTax: " + t.getCalculator().calculateTax()
                                    + "\nTotal Cost: " + t.getCalculator().calculateTotal());
                            quantityField.clear();
                        } catch (Exception ex) {
                            showError(ex.getMessage());
                        }
                    }
                });

            } catch (NumberFormatException ex) {
                showError("Please enter a valid quantity.");
            }
        });

        Button chartButton = new Button("Show Price Chart");
        chartButton.setOnAction(e -> {
            String selected = stockListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Please select a stock to view its chart.");
                return;
            }
            String symbol = selected.split(" ")[0];
            Stock stock = controller.getExchange().getStock(symbol);
            showPriceChart(stock);
        });

        Label detailsLabel = new Label("Select a stock to see details");
        stockListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    if (selected != null) {
                        String symbol = selected.split(" ")[0];
                        Stock stock = controller.getExchange().getStock(symbol);
                        detailsLabel.setText(
                                "Symbol: " + stock.getSymbol()
                                        + "  |  Company: " + stock.getCompany()
                                        + "  |  Current Price: " + stock.getSalesPrice()
                                        + "  |  Highest: " + stock.getHighestPrice()
                                        + "  |  Lowest: " + stock.getLowestPrice()
                                        + "  |  Change: " + stock.getLatestPriceChange());
                    }
                });

        HBox buyBar = new HBox(10, quantityLabel, quantityField, buyButton, chartButton);
        buyBar.setAlignment(Pos.CENTER_LEFT);

        VBox layout = new VBox(10, searchField, stockListView, detailsLabel, buyBar);
        layout.setPadding(new Insets(15));

        tab.setContent(layout);
        return tab;
    }

    /**
     * Builds the Portfolio tab showing the player's owned shares.
     */
    private Tab buildPortfolioTab() {
        Tab tab = new Tab("Portfolio");

        portfolioListView = new ListView<>();
        refreshPortfolioList();

        Button sellButton = new Button("Sell Selected");
        sellButton.setOnAction(e -> {
            int index = portfolioListView.getSelectionModel().getSelectedIndex();
            if (index < 0) {
                showError("Please select a share to sell.");
                return;
            }
            List<Share> shares = controller.getPlayer().getPortfolio().getShares();
            Share share = shares.get(index);

            BigDecimal proceeds = share.getStock().getSalesPrice()
                    .multiply(share.getQuantity());
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Sell " + share.getQuantity() + " units of "
                            + share.getStock().getSymbol()
                            + "?\nEstimated proceeds: " + proceeds,
                    ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    no.ntnu.idatx2003.millions.model.transaction.Transaction t =
                            controller.sellShare(share);
                    showInfo("Sale Receipt"
                            + "\n--------------------------"
                            + "\nStock: " + share.getStock().getSymbol()
                            + "\nQuantity: " + share.getQuantity()
                            + "\nGross: " + t.getCalculator().calculateGross()
                            + "\nCommission: " + t.getCalculator().calculateCommission()
                            + "\nTax: " + t.getCalculator().calculateTax()
                            + "\nNet Proceeds: " + t.getCalculator().calculateTotal());
                }
            });
        });

        VBox layout = new VBox(10, portfolioListView, sellButton);
        layout.setPadding(new Insets(15));

        tab.setContent(layout);
        return tab;
    }

    /**
     * Builds the Transactions tab showing all completed transactions.
     */
    /**
     * Builds the Transactions tab showing all completed transactions.
     */
    private Tab buildTransactionsTab() {
        Tab tab = new Tab("Transactions");

        TextField transactionSearchField = new TextField();
        transactionSearchField.setPromptText("Search by stock symbol or type...");

        transactionListView = new ListView<>();
        refreshTransactionList();

        // Filter the transaction list as the user types
        transactionSearchField.textProperty().addListener((obs, old, newVal) -> {
            String term = newVal.toLowerCase();
            transactionListView.setItems(FXCollections.observableArrayList(
                    controller.getPlayer().getTransactionArchive().getAllTransactions()
                            .stream()
                            .filter(t -> t.getShare().getStock().getSymbol()
                                    .toLowerCase().contains(term)
                                    || t.getClass().getSimpleName().toLowerCase().contains(term))
                            .map(t -> t.getClass().getSimpleName()
                                    + " | Week " + t.getWeek()
                                    + " | " + t.getShare().getStock().getSymbol()
                                    + " x" + t.getShare().getQuantity()
                                    + " | Total: " + t.getCalculator().calculateTotal())
                            .toList()));
        });

        VBox layout = new VBox(10,
                new Label("All completed transactions:"),
                transactionSearchField,
                transactionListView);
        layout.setPadding(new Insets(15));

        tab.setContent(layout);
        return tab;
    }

    /**
     * Builds the Market tab showing weekly gainers and losers.
     */
    private Tab buildMarketTab() {
        Tab tab = new Tab("Market");

        gainersListView = new ListView<>();
        losersListView = new ListView<>();
        refreshMarketLists();

        VBox gainersBox = new VBox(5, new Label("Top Gainers:"), gainersListView);
        VBox losersBox = new VBox(5, new Label("Top Losers:"), losersListView);

        HBox layout = new HBox(20, gainersBox, losersBox);
        layout.setPadding(new Insets(15));

        tab.setContent(layout);
        return tab;
    }

    /**
     * Opens a popup window showing the price history of the given stock as a line chart.
     *
     * @param stock the stock to display the chart for
     */
    private void showPriceChart(Stock stock) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Week");
        yAxis.setLabel("Price");

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(stock.getSymbol() + " - " + stock.getCompany());
        chart.setLegendVisible(false);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        List<BigDecimal> prices = stock.getHistoricalPrices();
        for (int i = 0; i < prices.size(); i++) {
            series.getData().add(new XYChart.Data<>(i + 1, prices.get(i)));
        }
        chart.getData().add(series);

        Stage chartStage = new Stage();
        chartStage.initModality(Modality.APPLICATION_MODAL);
        chartStage.setTitle("Price Chart - " + stock.getSymbol());
        chartStage.setScene(new Scene(chart, 600, 400));
        chartStage.show();
    }

    /**
     * Called automatically by the Observer pattern whenever the model changes.
     * Refreshes all displayed data.
     */
    @Override
    public void update() {
        updatePlayerInfo();
        refreshStockList();
        refreshPortfolioList();
        refreshTransactionList();
        refreshMarketLists();
        stage.setTitle("Millions - Week " + controller.getExchange().getWeek());
    }

    /**
     * Updates the player info labels at the top of the screen.
     */
    private void updatePlayerInfo() {
        moneyLabel.setText("Cash: " + controller.getPlayer().getMoney());
        netWorthLabel.setText("Net Worth: " + controller.getPlayer().getNetWorth());
        statusLabel.setText("Status: " + controller.getPlayer().getStatus());
        weekLabel.setText("Week: " + controller.getExchange().getWeek());
    }

    /**
     * Refreshes the stock list, applying any active search filter.
     */
    private void refreshStockList() {
        String search = searchField != null ? searchField.getText() : "";
        List<Stock> stocks = search.isBlank()
                ? controller.getExchange().findStocks("")
                : controller.getExchange().findStocks(search);
        stockListView.setItems(FXCollections.observableArrayList(
                stocks.stream()
                        .map(s -> s.getSymbol() + " - " + s.getCompany()
                                + " (" + s.getSalesPrice() + ")")
                        .toList()));
    }

    /**
     * Refreshes the portfolio list.
     */
    private void refreshPortfolioList() {
        portfolioListView.setItems(FXCollections.observableArrayList(
                controller.getPlayer().getPortfolio().getShares().stream()
                        .map(s -> s.getStock().getSymbol()
                                + " x" + s.getQuantity()
                                + " @ bought " + s.getPurchasePrice()
                                + " | now " + s.getStock().getSalesPrice())
                        .toList()));
    }

    /**
     * Refreshes the transaction history list.
     */
    private void refreshTransactionList() {
        transactionListView.setItems(FXCollections.observableArrayList(
                controller.getPlayer().getTransactionArchive().getAllTransactions()
                        .stream()
                        .map(t -> t.getClass().getSimpleName()
                                + " | Week " + t.getWeek()
                                + " | " + t.getShare().getStock().getSymbol()
                                + " x" + t.getShare().getQuantity()
                                + " | Total: " + t.getCalculator().calculateTotal())
                        .toList()));
    }

    /**
     * Refreshes the gainers and losers lists.
     */
    private void refreshMarketLists() {
        gainersListView.setItems(FXCollections.observableArrayList(
                controller.getExchange().getGainers(10).stream()
                        .map(s -> s.getSymbol() + " +" + s.getLatestPriceChange())
                        .toList()));
        losersListView.setItems(FXCollections.observableArrayList(
                controller.getExchange().getLosers(10).stream()
                        .map(s -> s.getSymbol() + " " + s.getLatestPriceChange())
                        .toList()));
    }

    /**
     * Shows an error alert dialog.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an information alert dialog.
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
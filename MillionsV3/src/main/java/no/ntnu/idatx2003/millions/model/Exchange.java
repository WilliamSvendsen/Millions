package no.ntnu.idatx2003.millions.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import no.ntnu.idatx2003.millions.model.transaction.Purchase;
import no.ntnu.idatx2003.millions.model.transaction.Sale;
import no.ntnu.idatx2003.millions.model.transaction.Transaction;
import java.io.IOException;
import java.util.ArrayList;
import no.ntnu.idatx2003.millions.model.file.StockFileHandler;
import java.util.Comparator;
import no.ntnu.idatx2003.millions.model.transaction.TransactionFactory;
import no.ntnu.idatx2003.millions.observer.Observable;
import no.ntnu.idatx2003.millions.observer.Observer;
import java.util.HashSet;
import java.util.Set;
import no.ntnu.idatx2003.millions.model.event.RandomEventService;

/**
 * Represents a stock exchange where players can buy and sell stocks.
 *
 * <p>The exchange keeps track of all listed stocks and the current trading week.
 * Each time the market advances to a new week, every stock's price is updated
 * with a small random change. Never more than 5% up or down from the previous price.</p>
 */

public class Exchange implements Observable {

  // The set of observers to notify when the exchange state changes
  private final Set<Observer> observers;

  // The maximum price change allowed per week, expressed as a fraction.
  // 0.05 means prices can move at most +-5% each week.
  // static means this value is shared across all Exchange instances, not per object.
  // final means it never changes.
  private static final BigDecimal MAX_WEEKLY_CHANGE = new BigDecimal("0.05");

  // The name of this exchange, for example "Oslo Børs" or "Nasdaq"
  private final String name;

  // Stocks stored in a Map (key=symbol, value=Stock object).
  // A Map allows instant lookup by symbol, stockMap.get("AAPL") is direct,
  // rather than searching through a list one by one.
  private final Map<String, Stock> stockMap;

  // Used to generate random price changes each week
  private final Random random;

  // The current trading week number, starts at 1 and increments with each advance()
  private int week;

  // Factory used to create purchase and sale transactions
  private final TransactionFactory transactionFactory;

  // Service that generates random market events each week
  private final RandomEventService eventService;

  /**
   * Creates a new Exchange with the given name and initial list of stocks.
   *
   * @param name   the name of the exchange
   * @param stocks the stocks listed on this exchange
   * @throws IllegalArgumentException if name is blank or stocks is null
   */
  public Exchange(String name, List<Stock> stocks) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Exchange name cannot be null or blank");
    }
    if (stocks == null) {
      throw new IllegalArgumentException("Stocks list cannot be null");
    }
    this.name = name;
    this.stockMap = new HashMap<>();
    // forEach goes through each stock in the list and puts it in the map,
    // using the stock's symbol as the key for fast lookup later
    stocks.forEach(s -> stockMap.put(s.getSymbol(), s));
    this.random = new Random();
    this.transactionFactory = new TransactionFactory();
    this.observers = new HashSet<>();
    this.eventService = new RandomEventService();
    // Trading always starts at week 1
    this.week = 1;
  }

  /**
   * Returns the name of this exchange.
   *
   * @return exchange name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the current trading week number.
   *
   * @return current week
   */
  public int getWeek() {
    return week;
  }

  /**
   * Returns true if a stock with the given symbol is listed on this exchange.
   *
   * @param symbol the ticker symbol to check
   * @return true if the stock exists
   */
  public boolean hasStock(String symbol) {
    // Return false immediately for null rather than throwing an exception,
    // since "does null exist?" is a valid question with a clear answer: no
    if (symbol == null) {
      return false;
    }
    // toUpperCase() ensures the lookup works regardless of letter case
    return stockMap.containsKey(symbol.toUpperCase());
  }

  /**
   * Returns the stock for the given symbol.
   *
   * @param symbol the ticker symbol
   * @return the matching Stock
   * @throws IllegalArgumentException if the symbol is null or not listed
   */
  public Stock getStock(String symbol) {
    // Reuse hasStock() to check - if not found, throw with a helpful message
    if (symbol == null || !hasStock(symbol)) {
      throw new IllegalArgumentException("Stock not found: " + symbol);
    }
    // Direct map lookup by symbol - fast regardless of how many stocks exist
    return stockMap.get(symbol.toUpperCase());
  }

  /**
   * Returns all stocks whose symbol or company name contains the search term,
   * case-insensitive.
   *
   * @param searchTerm the term to search for
   * @return list of matching stocks (may be empty)
   * @throws IllegalArgumentException if searchTerm is null
   */
  public List<Stock> findStocks(String searchTerm) {
    if (searchTerm == null) {
      throw new IllegalArgumentException("Search term cannot be null");
    }
    // Convert to lowercase once here rather than on every comparison
    String term = searchTerm.toLowerCase();
    // Stream through all stocks in the map and keep ones where either
    // the symbol or company name contains the search term
    return stockMap.values().stream()
            .filter(s -> s.getSymbol().toLowerCase().contains(term)
                    || s.getCompany().toLowerCase().contains(term))
            .collect(Collectors.toList());
  }

  /**
   * Executes a stock purchase for the given player.
   * Creates a Share at the current price, wraps it in a Purchase transaction,
   * commits it, and returns the completed transaction.
   *
   * @param symbol   the ticker symbol of the stock to buy
   * @param quantity the number of units to buy (must be positive)
   * @param player   the player making the purchase
   * @return the committed Purchase transaction
   * @throws IllegalArgumentException if the symbol is not listed or quantity is not positive
   * @throws IllegalStateException    if the player has insufficient funds
   */
  public Transaction buy(String symbol, BigDecimal quantity, Player player) {
    if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");

    }
    // Look up the stock - throws if symbol not found
    Stock stock = getStock(symbol);

    // Create the Share, locking in the current price as the purchase price.
    // stock.getSalesPrice() reads the most recent price from the stock's price list.
    Share share = new Share(stock, quantity, stock.getSalesPrice());

    // Wrap the share in a Purchase transaction for the current week
    Purchase purchase = transactionFactory.createPurchase(share, week);

    // commit() validates funds, deducts cost, adds share to portfolio, archives transaction
    purchase.commit(player);

    notifyObservers();

    // Return the completed transaction so the caller has a reference if needed
    return purchase;
  }

  /**
   * Executes a stock sale for the given player.
   * The share must already exist in the player's portfolio.
   *
   * @param share  the share to sell
   * @param player the player making the sale
   * @return the committed Sale transaction
   * @throws IllegalArgumentException if share is null
   * @throws IllegalStateException    if the player does not own the share
   */
  public Transaction sell(Share share, Player player) {
    if (share == null) {
      throw new IllegalArgumentException("Share cannot be null");
    }
    // Wrap the share in a Sale transaction for the current week
    Sale sale = transactionFactory.createSale(share, week);

    // commit() validates ownership, adds proceeds, removes share from portfolio, archives
    sale.commit(player);

    notifyObservers();

    return sale;
  }

  /**
   * Advances the exchange to the next trading week.
   * Increments the week counter, updates prices, and processes random events.
   * Returns a list of event messages that occurred this week.
   *
   * @return list of random event messages (may be empty)
   */
  public List<String> advance() {
    week++;

    // Update all stock prices with normal random fluctuation
    stockMap.values().forEach(stock -> {
      double changePercent = (random.nextDouble() * 2 - 1) * MAX_WEEKLY_CHANGE.doubleValue();
      BigDecimal multiplier = BigDecimal.ONE.add(
              new BigDecimal(changePercent).setScale(6, RoundingMode.HALF_UP));
      BigDecimal newPrice = stock.getSalesPrice()
              .multiply(multiplier)
              .setScale(2, RoundingMode.HALF_UP)
              .max(new BigDecimal("0.01"));
      stock.addNewSalesPrice(newPrice);
    });

    // Process random events on top of normal price changes
    List<String> events = eventService.processEvents(
            new ArrayList<>(stockMap.values()));

    notifyObservers();
    return events;
  }

  /**
   * Returns the top stocks with the largest positive price change since last week.
   *
   * @param limit maximum number of stocks to return
   * @return list of top gaining stocks, sorted by price change descending
   */
  public List<Stock> getGainers(int limit) {
    // Filter to only stocks with a positive price change,
    // sort by largest change first, then take only the top 'limit' results
    return stockMap.values().stream()
            .filter(s -> s.getLatestPriceChange().compareTo(BigDecimal.ZERO) > 0)
            .sorted(Comparator.comparing(Stock::getLatestPriceChange).reversed())
            .limit(limit)
            .collect(Collectors.toList());
  }

  /**
   * Returns the top stocks with the largest negative price change since last week.
   *
   * @param limit maximum number of stocks to return
   * @return list of top losing stocks, sorted by price change ascending
   */
  public List<Stock> getLosers(int limit) {
    // Filter to only stocks with a negative price change,
    // sort by smallest change first (most negative), then take only the top 'limit' results
    return stockMap.values().stream()
            .filter(s -> s.getLatestPriceChange().compareTo(BigDecimal.ZERO) < 0)
            .sorted(Comparator.comparing(Stock::getLatestPriceChange))
            .limit(limit)
            .collect(Collectors.toList());
  }
  /**
   * Registers an observer to be notified when the exchange state changes.
   *
   * @param observer the observer to register
   */
  @Override
  public void addObserver(Observer observer) {
    observers.add(observer);
  }

  /**
   * Removes a previously registered observer.
   *
   * @param observer the observer to remove
   */
  @Override
  public void removeObserver(Observer observer) {
    observers.remove(observer);
  }

  /**
   * Notifies all registered observers that the state has changed.
   */
  @Override
  public void notifyObservers() {
    // Call update() on every registered observer
    observers.forEach(Observer::update);
  }

  /**
   * Increments the week counter without updating prices or notifying observers.
   * Used only when restoring a saved game state.
   */
  public void advanceWeekCounter() {
    week++;
  }

  /**
   * Loads stocks from a file and adds them to this exchange.
   *
   * @param filePath path to the file to load from
   * @param fileHandler the file handler to use (e.g. CsvStockFileHandler)
   * @throws IOException if the file cannot be read
   */
  public void loadStocksFromFile(String filePath, StockFileHandler fileHandler)
          throws IOException {
    List<Stock> loaded = fileHandler.readStocks(filePath);
    loaded.forEach(s -> stockMap.put(s.getSymbol(), s));
  }

  /**
   * Saves all currently listed stocks to a file.
   *
   * @param filePath path to the file to save to
   * @param fileHandler the file handler to use (for example CsvStockFileHandler)
   * @throws IOException if the file cannot be written
   */
  public void saveStocksToFile(String filePath, StockFileHandler fileHandler)
          throws IOException {
    fileHandler.writeStocks(new ArrayList<>(stockMap.values()), filePath);
  }
}
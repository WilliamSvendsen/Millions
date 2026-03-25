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

/**
 * Represents a stock exchange where players can buy and sell stocks.
 *
 * <p>The exchange keeps track of all listed stocks and the current trading week.
 * Each time the market advances to a new week, every stock's price is updated
 * with a small random change. Mever more than 5% up or down from the previous price.</p>
 */

public class Exchange {

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
    Purchase purchase = new Purchase(share, week);

    // commit() validates funds, deducts cost, adds share to portfolio, archives transaction
    purchase.commit(player);

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
    Sale sale = new Sale(share, week);

    // commit() validates ownership, adds proceeds, removes share from portfolio, archives
    sale.commit(player);

    return sale;
  }

  /**
   * Advances the exchange to the next trading week.
   * Increments the week counter and pushes a new randomly adjusted price
   * (within +-5%) onto every listed stock's price history.
   */
  public void advance() {
    // Move to the next week
    week++;

    // Update the price of every stock in the exchange
    stockMap.values().forEach(stock -> {
      // random.nextDouble() produces a number between 0.0 and 1.0.
      // Multiplying by 2 and subtracting 1 shifts the range to -1.0 to +1.0.
      // Multiplying by MAX_WEEKLY_CHANGE (0.05) scales it to -0.05 to +0.05,
      // meaning a maximum price change of +-5%.
      double changePercent = (random.nextDouble() * 2 - 1) * MAX_WEEKLY_CHANGE.doubleValue();

      // Adding 1 turns the change into a multiplier.
      // e.g. -0.03 becomes 0.97 (a 3% drop), +0.02 becomes 1.02 (a 2% rise).
      BigDecimal multiplier = BigDecimal.ONE.add(
              new BigDecimal(changePercent).setScale(6, RoundingMode.HALF_UP));

      // Apply the multiplier to the current price, round to 2 decimal places,
      // and ensure the price never drops below 0.01 (a stock cant be worth nothing)
      BigDecimal newPrice = stock.getSalesPrice()
              .multiply(multiplier)
              .setScale(2, RoundingMode.HALF_UP)
              .max(new BigDecimal("0.01"));

      // Push the new price onto the stock's price history list,
      // making it the new current price
      stock.addNewSalesPrice(newPrice);
    });
  }
}
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
 * <p>Manages listed stocks, tracks the current trading week, and handles
 * weekly price updates via {@link #advance()}. Prices fluctuate randomly
 * each week by up to +-5%.</p>
 */
public class Exchange {

  private static final BigDecimal MAX_WEEKLY_CHANGE = new BigDecimal("0.05");

  private final String name;
  private final Map<String, Stock> stockMap;
  private final Random random;
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
    stocks.forEach(s -> stockMap.put(s.getSymbol(), s));
    this.random = new Random();
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
    if (symbol == null) {
      return false;
    }
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
    if (symbol == null || !hasStock(symbol)) {
      throw new IllegalArgumentException("Stock not found: " + symbol);
    }
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
    String term = searchTerm.toLowerCase();
    return stockMap.values().stream()
        .filter(s -> s.getSymbol().toLowerCase().contains(term)
            || s.getCompany().toLowerCase().contains(term))
        .collect(Collectors.toList());
  }

  /**
   * Executes a stock purchase for the given player.
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
    Stock stock = getStock(symbol);
    Share share = new Share(stock, quantity, stock.getSalesPrice());
    Purchase purchase = new Purchase(share, week);
    purchase.commit(player);
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
    Sale sale = new Sale(share, week);
    sale.commit(player);
    return sale;
  }

  /**
   * Advances the exchange to the next trading week.
   * Increments the week counter and pushes a new randomly adjusted price
   * (within +-5%) onto every listed stock's price history.
   */
  public void advance() {
    week++;
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
  }
}

package no.ntnu.idatx2003.millions.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a stock listed on an exchange.
 *
 * <p>A stock has a unique ticker symbol (e.g. "AAPL"), a company name,
 * and a list of prices. A new price is appended each week when the market
 * advances. The current sales price is always the most recently added price.</p>
 */
public class Stock {

  // The unique ticker symbol for this stock, e.g. "AAPL" for Apple.
  // final means this can never be changed after the object is created.
  private final String symbol;

  // The full name of the company, e.g. "Apple Inc."
  private final String company;

  // A list storing every price this stock has ever had, in order.
  // The current price is always the last entry in this list.
  // We use BigDecimal instead of double to avoid floating point precision
  // errors when doing monetary calculations.
  private final List<BigDecimal> prices;

  /**
   * Creates a new Stock with an initial sales price.
   *
   * @param symbol     unique ticker symbol, e.g. "AAPL"
   * @param company    full company name
   * @param salesPrice initial price (must be non-negative)
   * @throws IllegalArgumentException if symbol or company is blank, or price is negative
   */
  public Stock(String symbol, String company, BigDecimal salesPrice) {
    // Reject null or blank symbols - every stock must have a valid identifier
    if (symbol == null || symbol.isBlank()) {
      throw new IllegalArgumentException("Symbol cannot be null or blank");
    }
    // Reject null or blank company names
    if (company == null || company.isBlank()) {
      throw new IllegalArgumentException("Company cannot be null or blank");
    }
    // Reject null or negative prices - a stock cannot have a negative value
    // compareTo returns a negative number if salesPrice is less than ZERO
    if (salesPrice == null || salesPrice.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Sales price cannot be null or negative");
    }
    // toUpperCase() ensures "aapl" and "AAPL" are treated as the same symbol
    this.symbol = symbol.toUpperCase();
    this.company = company;
    // Create a new empty list to hold prices over time
    this.prices = new ArrayList<>();
    // Add the starting price as the first entry in the price history
    this.prices.add(salesPrice);
  }

  /**
   * Returns the ticker symbol.
   *
   * @return ticker symbol
   */
  public String getSymbol() {
    return symbol;
  }

  /**
   * Returns the company name.
   *
   * @return company name
   */
  public String getCompany() {
    return company;
  }

  /**
   * Returns the current sales price (the most recently added price).
   *
   * @return current sales price
   */
  public BigDecimal getSalesPrice() {
    // prices.size() gives the total number of entries in the list.
    // Since lists are zero-indexed (first item is at index 0),
    // the last item is always at index size - 1.
    return prices.get(prices.size() - 1);
  }

  /**
   * Adds a new sales price to the price history.
   * Called each week by Exchange when prices are updated.
   *
   * @param price new price (must be non-negative)
   * @throws IllegalArgumentException if price is null or negative
   */
  public void addNewSalesPrice(BigDecimal price) {
    // Reject null or negative prices before adding to the list
    if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Price cannot be null or negative");
    }
    // Appending to the end of the list makes this the new current price,
    // since getSalesPrice() always reads the last entry
    prices.add(price);
  }

  // equals() and hashCode() allow Java to correctly compare two Stock objects.
  // Two stocks are considered equal if they have the same symbol,
  // regardless of price or company name.
  @Override
  public boolean equals(Object object) {
    // If both references point to the exact same object in memory, they are equal
    if (this == object) {
      return true;
    }
    // If the other object is not a Stock at all, they cannot be equal
    if (!(object instanceof Stock stock)) {
      return false;
    }
    // Two stocks are equal if and only if their symbols match
    return symbol.equals(stock.symbol);
  }

  @Override
  public int hashCode() {
    // Generates a unique-ish number based on the symbol,
    // used internally by Java collections like HashMap
    return Objects.hash(symbol);
  }

  // toString() produces a human-readable description of this stock,
  // e.g. "AAPL - Apple Inc. (150.00)"
  @Override
  public String toString() {
    return symbol + " - " + company + " (" + getSalesPrice() + ")";
  }
}
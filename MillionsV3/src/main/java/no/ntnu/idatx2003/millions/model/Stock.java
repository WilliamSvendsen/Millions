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

  private final String symbol;
  private final String company;
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
    if (symbol == null || symbol.isBlank()) {
      throw new IllegalArgumentException("Symbol cannot be null or blank");
    }
    if (company == null || company.isBlank()) {
      throw new IllegalArgumentException("Company cannot be null or blank");
    }
    if (salesPrice == null || salesPrice.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Sales price cannot be null or negative");
    }
    this.symbol = symbol.toUpperCase();
    this.company = company;
    this.prices = new ArrayList<>();
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
    if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Price cannot be null or negative");
    }
    prices.add(price);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof Stock stock)) {
      return false;
    }
    return symbol.equals(stock.symbol);
  }

  @Override
  public int hashCode() {
    return Objects.hash(symbol);
  }

  @Override
  public String toString() {
    return symbol + " - " + company + " (" + getSalesPrice() + ")";
  }
}

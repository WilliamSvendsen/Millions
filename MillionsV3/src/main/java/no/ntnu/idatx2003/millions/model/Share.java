package no.ntnu.idatx2003.millions.model;

import java.math.BigDecimal;

/**
 * Represents an owned stake in a stock resulting from a single purchase.
 *
 * <p>A Share records which stock was bought, how many units (quantity),
 * and what price was paid per unit at the time of purchase. The purchase
 * price is stored separately from the stock's current price so that
 * profit and loss can be calculated correctly at sale time.</p>
 */
public class Share {

  private final Stock stock;
  private final BigDecimal quantity;
  private final BigDecimal purchasePrice;

  /**
   * Creates a new Share.
   *
   * @param stock         the stock this share represents
   * @param quantity      number of units purchased (must be positive)
   * @param purchasePrice price per unit at the time of purchase (must be non-negative)
   * @throws IllegalArgumentException if any argument is invalid
   */
  public Share(Stock stock, BigDecimal quantity, BigDecimal purchasePrice) {
    if (stock == null) {
      throw new IllegalArgumentException("Stock cannot be null");
    }
    if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
    if (purchasePrice == null || purchasePrice.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Purchase price cannot be null or negative");
    }
    this.stock = stock;
    this.quantity = quantity;
    this.purchasePrice = purchasePrice;
  }

  /**
   * Returns the stock this share belongs to.
   *
   * @return the stock
   */
  public Stock getStock() {
    return stock;
  }

  /**
   * Returns the number of units held.
   *
   * @return quantity
   */
  public BigDecimal getQuantity() {
    return quantity;
  }

  /**
   * Returns the price per unit paid at purchase time.
   *
   * @return purchase price per unit
   */
  public BigDecimal getPurchasePrice() {
    return purchasePrice;
  }

  @Override
  public String toString() {
    return "Share{"
        + "stock=" + stock.getSymbol()
        + ", quantity=" + quantity
        + ", purchasePrice=" + purchasePrice
        + '}';
  }
}

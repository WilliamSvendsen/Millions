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

  // The stock this share belongs to, e.g. the Apple stock object
  private final Stock stock;

  // How many units of the stock were purchased in this transaction.
  // BigDecimal is used to allow fractional quantities if needed,
  // and to keep precision consistent with the rest of the monetary values.
  private final BigDecimal quantity;

  // The price paid per unit at the exact moment of purchase.
  // This is stored permanently and never changes, even as the stock's
  // current price moves up and down over time.
  // This is essential for calculating profit/loss and tax at sale time.
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
    // A share must be linked to an actual stock
    if (stock == null) {
      throw new IllegalArgumentException("Stock cannot be null");
    }
    // compareTo returns 0 if equal and negative if less than - so <= 0 means
    // "zero or negative", both of which are invalid quantities
    if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
    // Purchase price can be zero (e.g. a free stock) but never negative
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
   * This never changes, it reflects what was paid, not the current market value.
   *
   * @return purchase price per unit
   */
  public BigDecimal getPurchasePrice() {
    return purchasePrice;
  }

  // Produces a readable summary of this share, e.g.
  // "Share{stock=AAPL, quantity=10, purchasePrice=150.00}"
  @Override
  public String toString() {
    return "Share{"
            + "stock=" + stock.getSymbol()
            + ", quantity=" + quantity
            + ", purchasePrice=" + purchasePrice
            + '}';
  }
}
package no.ntnu.idatx2003.millions.model.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import no.ntnu.idatx2003.millions.model.Share;

/**
 * Calculates the proceeds breakdown for a stock sale.
 *
 * <ul>
 *   <li>Gross = currentSalesPrice x quantity</li>
 *   <li>Commission = 1% of gross</li>
 *   <li>Gain = gross - commission - (purchasePrice x quantity)</li>
 *   <li>Tax = 30% of gain (only when gain is positive, i.e. sold at a profit)</li>
 *   <li>Total = gross - commission - tax</li>
 * </ul>
 */
public class SaleCalculator implements TransactionCalculator {

  // 1% commission rate on sales (higher than the 0.5% on purchases)
  private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.01");

  // 30% tax rate applied to any profit made on a sale
  private static final BigDecimal TAX_RATE = new BigDecimal("0.30");

  // All monetary results are rounded to 2 decimal places
  private static final int SCALE = 2;

  // The original price paid per unit when this share was purchased.
  // Used to calculate how much profit was made.
  private final BigDecimal purchasePrice;

  // The current market price per unit at the time of the sale.
  // This is what determines the gross value of the sale.
  private final BigDecimal salesPrice;

  // How many units are being sold
  private final BigDecimal quantity;

  /**
   * Creates a SaleCalculator for the given share.
   * Reads purchasePrice, current salesPrice, and quantity from the share.
   *
   * @param share the share being sold
   * @throws IllegalArgumentException if share is null
   */
  public SaleCalculator(Share share) {
    if (share == null) {
      throw new IllegalArgumentException("Share cannot be null");
    }
    this.purchasePrice = share.getPurchasePrice();
    // getSalesPrice() reaches through the share to the stock and reads
    // the stock's current (most recently added) price
    this.salesPrice = share.getStock().getSalesPrice();
    this.quantity = share.getQuantity();
  }

  // Gross = current sales price x quantity
  // e.g. selling 10 units at the current price of 200.00 = 2000.00
  @Override
  public BigDecimal calculateGross() {
    return salesPrice.multiply(quantity).setScale(SCALE, RoundingMode.HALF_UP);
  }

  // Commission = 1% of gross
  // e.g. 1% of 2000.00 = 20.00
  @Override
  public BigDecimal calculateCommission() {
    return calculateGross().multiply(COMMISSION_RATE).setScale(SCALE, RoundingMode.HALF_UP);
  }

  // Tax = 30% of profit, but only if a profit was actually made.
  // Profit = gross - commission - what was originally paid for the shares.
  // If sold at a loss or break-even, tax is 0.
  @Override
  public BigDecimal calculateTax() {
    // What the player originally paid for these shares in total
    BigDecimal purchaseCost =
            purchasePrice.multiply(quantity).setScale(SCALE, RoundingMode.HALF_UP);

    // Gain = what you receive now minus what you paid then
    // subtract() is the BigDecimal equivalent of the - operator
    BigDecimal gain =
            calculateGross().subtract(calculateCommission()).subtract(purchaseCost);

    // If gain is zero or negative (sold at a loss), no tax is owed
    // compareTo returns <= 0 when gain is zero or negative
    if (gain.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
    }

    // Otherwise charge 30% of the gain as tax
    return gain.multiply(TAX_RATE).setScale(SCALE, RoundingMode.HALF_UP);
  }

  // Total proceeds = gross - commission - tax
  // This is the actual amount added to the player's balance after the sale
  // e.g. 2000.00 - 20.00 - 294.00 = 1686.00
  @Override
  public BigDecimal calculateTotal() {
    return calculateGross()
            .subtract(calculateCommission())
            .subtract(calculateTax())
            .setScale(SCALE, RoundingMode.HALF_UP);
  }
}
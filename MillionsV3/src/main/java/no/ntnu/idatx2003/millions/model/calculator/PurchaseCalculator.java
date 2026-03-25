package no.ntnu.idatx2003.millions.model.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import no.ntnu.idatx2003.millions.model.Share;

/**
 * Calculates the cost breakdown for a stock purchase.
 *
 * <ul>
 *   <li>Gross = purchasePrice x quantity</li>
 *   <li>Commission = 0.5% of gross</li>
 *   <li>Tax = 0 (no tax on purchases)</li>
 *   <li>Total = gross + commission</li>
 * </ul>
 */
public class PurchaseCalculator implements TransactionCalculator {

  // 0.5% expressed as a decimal. Stored as a String to avoid floating point
  // precision issues - new BigDecimal("0.005") is exact, new BigDecimal(0.005) is not.
  private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.005");

  // All monetary results are rounded to 2 decimal places (e.g. 10.556 becomes 10.56)
  private static final int SCALE = 2;

  // The price paid per unit, captured from the Share at construction time
  private final BigDecimal purchasePrice;

  // How many units were purchased, captured from the Share at construction time
  private final BigDecimal quantity;

  /**
   * Creates a PurchaseCalculator for the given share.
   * Reads purchasePrice and quantity from the share and stores them locally.
   *
   * @param share the share being purchased
   * @throws IllegalArgumentException if share is null
   */
  public PurchaseCalculator(Share share) {
    if (share == null) {
      throw new IllegalArgumentException("Share cannot be null");
    }
    // Capture the values we need from the share upfront
    this.purchasePrice = share.getPurchasePrice();
    this.quantity = share.getQuantity();
  }

  // Gross = purchasePrice x quantity
  // e.g. 10 units at 100.00 each = 1000.00
  // setScale(2, HALF_UP) rounds to 2 decimal places using standard rounding
  @Override
  public BigDecimal calculateGross() {
    return purchasePrice.multiply(quantity).setScale(SCALE, RoundingMode.HALF_UP);
  }

  // Commission = 0.5% of gross
  // e.g. 0.5% of 1000.00 = 5.00
  @Override
  public BigDecimal calculateCommission() {
    return calculateGross().multiply(COMMISSION_RATE).setScale(SCALE, RoundingMode.HALF_UP);
  }

  // No tax is charged on purchases - always returns zero
  // setScale ensures the zero is formatted consistently as 0.00
  @Override
  public BigDecimal calculateTax() {
    return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
  }

  // Total cost = gross + commission
  // e.g. 1000.00 + 5.00 = 1005.00
  // This is the amount deducted from the player's balance
  @Override
  public BigDecimal calculateTotal() {
    return calculateGross().add(calculateCommission()).setScale(SCALE, RoundingMode.HALF_UP);
  }
}
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

  private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.005");
  private static final int SCALE = 2;

  private final BigDecimal purchasePrice;
  private final BigDecimal quantity;

  /**
   * Creates a PurchaseCalculator for the given share.
   *
   * @param share the share being purchased
   * @throws IllegalArgumentException if share is null
   */
  public PurchaseCalculator(Share share) {
    if (share == null) {
      throw new IllegalArgumentException("Share cannot be null");
    }
    this.purchasePrice = share.getPurchasePrice();
    this.quantity = share.getQuantity();
  }

  @Override
  public BigDecimal calculateGross() {
    return purchasePrice.multiply(quantity).setScale(SCALE, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal calculateCommission() {
    return calculateGross().multiply(COMMISSION_RATE).setScale(SCALE, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal calculateTax() {
    return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal calculateTotal() {
    return calculateGross().add(calculateCommission()).setScale(SCALE, RoundingMode.HALF_UP);
  }
}

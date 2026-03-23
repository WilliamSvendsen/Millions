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
 *   <li>Tax = 30% of gain (only when gain is positive)</li>
 *   <li>Total = gross - commission - tax</li>
 * </ul>
 */
public class SaleCalculator implements TransactionCalculator {

  private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.01");
  private static final BigDecimal TAX_RATE = new BigDecimal("0.30");
  private static final int SCALE = 2;

  private final BigDecimal purchasePrice;
  private final BigDecimal salesPrice;
  private final BigDecimal quantity;

  /**
   * Creates a SaleCalculator for the given share.
   *
   * @param share the share being sold
   * @throws IllegalArgumentException if share is null
   */
  public SaleCalculator(Share share) {
    if (share == null) {
      throw new IllegalArgumentException("Share cannot be null");
    }
    this.purchasePrice = share.getPurchasePrice();
    this.salesPrice = share.getStock().getSalesPrice();
    this.quantity = share.getQuantity();
  }

  @Override
  public BigDecimal calculateGross() {
    return salesPrice.multiply(quantity).setScale(SCALE, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal calculateCommission() {
    return calculateGross().multiply(COMMISSION_RATE).setScale(SCALE, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal calculateTax() {
    BigDecimal purchaseCost =
        purchasePrice.multiply(quantity).setScale(SCALE, RoundingMode.HALF_UP);
    BigDecimal gain =
        calculateGross().subtract(calculateCommission()).subtract(purchaseCost);
    if (gain.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
    }
    return gain.multiply(TAX_RATE).setScale(SCALE, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal calculateTotal() {
    return calculateGross()
        .subtract(calculateCommission())
        .subtract(calculateTax())
        .setScale(SCALE, RoundingMode.HALF_UP);
  }
}

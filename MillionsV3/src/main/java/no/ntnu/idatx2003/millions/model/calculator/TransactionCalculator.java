package no.ntnu.idatx2003.millions.model.calculator;

import java.math.BigDecimal;

/**
 * Defines the calculations required for any financial transaction.
 *
 * <p>Implementations handle the specific rules for purchases vs. sales,
 * covering gross value, commission, tax, and total cost or proceeds.</p>
 */
public interface TransactionCalculator {

  /**
   * Calculates the gross value before any fees or tax.
   *
   * @return gross value
   */
  BigDecimal calculateGross();

  /**
   * Calculates the commission (brokerage fee) for this transaction.
   *
   * @return commission amount
   */
  BigDecimal calculateCommission();

  /**
   * Calculates the tax owed on this transaction.
   *
   * @return tax amount
   */
  BigDecimal calculateTax();

  /**
   * Calculates the total value after fees and tax.
   * For a purchase this is the total cost to the buyer.
   * For a sale this is the net proceeds received.
   *
   * @return total value
   */
  BigDecimal calculateTotal();
}

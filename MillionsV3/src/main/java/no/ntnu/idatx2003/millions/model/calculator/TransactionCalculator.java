package no.ntnu.idatx2003.millions.model.calculator;

import java.math.BigDecimal;

/**
 * Defines the calculations required for any financial transaction.
 *
 * <p>This is an interface, it defines a contract that both PurchaseCalculator
 * and SaleCalculator must fulfil. It contains no logic of its own, only the
 * four method signatures that any implementing class must provide.
 * This allows Transaction to work with either calculator without needing
 * to know which one it is.</p>
 */
public interface TransactionCalculator {

  /**
   * Calculates the gross value before any fees or tax.
   * For a purchase: purchasePrice x quantity.
   * For a sale: currentSalesPrice x quantity.
   *
   * @return gross value
   */
  BigDecimal calculateGross();

  /**
   * Calculates the commission (brokerage fee) charged for this transaction.
   * This is a percentage of the gross value.
   * Purchase rate: 0.5%. Sale rate: 1%.
   *
   * @return commission amount
   */
  BigDecimal calculateCommission();

  /**
   * Calculates the tax owed on this transaction.
   * No tax is charged on purchases.
   * On sales, 30% tax is charged on any profit made.
   *
   * @return tax amount
   */
  BigDecimal calculateTax();

  /**
   * Calculates the final total value after all fees and tax.
   * For a purchase this is the total cost deducted from the player's balance.
   * For a sale this is the net amount added to the player's balance.
   *
   * @return total value
   */
  BigDecimal calculateTotal();
}
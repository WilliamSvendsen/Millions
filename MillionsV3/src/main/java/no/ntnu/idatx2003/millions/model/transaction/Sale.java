package no.ntnu.idatx2003.millions.model.transaction;

import no.ntnu.idatx2003.millions.model.Player;
import no.ntnu.idatx2003.millions.model.Share;
import no.ntnu.idatx2003.millions.model.calculator.SaleCalculator;

/**
 * Represents a stock sale transaction.
 *
 * <p>When committed, the net sale proceeds are added to the player's balance,
 * the share is removed from their portfolio, and the transaction is recorded
 * in their archive.</p>
 *
 * <p>Commit will fail if the player does not own the share being sold, or if
 * this transaction has already been committed.</p>
 */
public class Sale extends Transaction {

  /**
   * Creates a Sale transaction.
   * Automatically creates a SaleCalculator for the given share
   * and passes it up to the Transaction base class via super().
   *
   * @param share the share being sold
   * @param week  the trading week number
   */
  public Sale(Share share, int week) {
    // super() calls the Transaction constructor with the share, week,
    // and a new SaleCalculator built from the same share
    super(share, week, new SaleCalculator(share));
  }

  /**
   * Commits the sale: adds net proceeds to the player's balance,
   * removes the share from the player's portfolio, and archives the transaction.
   *
   * @param player the player making the sale
   * @throws IllegalArgumentException if player is null
   * @throws IllegalStateException    if already committed or player does not own the share
   */
  @Override
  public void commit(Player player) {
    if (player == null) {
      throw new IllegalArgumentException("Player cannot be null");
    }
    // Prevent the same transaction from being committed twice
    if (committed) {
      throw new IllegalStateException("This transaction has already been committed");
    }
    // Verify the player actually owns this share before attempting the sale.
    // contains() checks the portfolio for this exact Share object.
    // The ! at the front means "if the portfolio does NOT contain the share"
    if (!player.getPortfolio().contains(getShare())) {
      throw new IllegalStateException("Player does not own the share being sold");
    }

    // All checks passed - now execute the three steps of the sale:

    // Step 1: Add the sale proceeds to the player's cash balance.
    // getCalculator() returns the SaleCalculator, calculateTotal() returns
    // gross minus commission minus tax.
    player.addMoney(getCalculator().calculateTotal());

    // Step 2: Remove the share from the player's portfolio - they no longer own it
    player.getPortfolio().removeShare(getShare());

    // Step 3: Record this transaction in the player's archive.
    // 'this' refers to this Sale object itself - it archives itself.
    committed = true;
    player.getTransactionArchive().add(this);
  }
}
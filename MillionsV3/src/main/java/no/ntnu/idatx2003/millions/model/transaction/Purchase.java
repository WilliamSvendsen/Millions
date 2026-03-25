package no.ntnu.idatx2003.millions.model.transaction;

import no.ntnu.idatx2003.millions.model.Player;
import no.ntnu.idatx2003.millions.model.Share;
import no.ntnu.idatx2003.millions.model.calculator.PurchaseCalculator;

/**
 * Represents a stock purchase transaction.
 *
 * <p>When committed, the total purchase cost is deducted from the player's
 * balance, the share is added to their portfolio, and the transaction is
 * recorded in their archive.</p>
 *
 * <p>Commit will fail if the player has insufficient funds or if this
 * transaction has already been committed.</p>
 */
public class Purchase extends Transaction {

  /**
   * Creates a Purchase transaction.
   * Automatically creates a PurchaseCalculator for the given share
   * and passes it up to the Transaction base class via super().
   *
   * @param share the share being purchased
   * @param week  the trading week number
   */
  public Purchase(Share share, int week) {
    // super() calls the Transaction constructor with the share, week,
    // and a new PurchaseCalculator built from the same share
    super(share, week, new PurchaseCalculator(share));
  }

  /**
   * Commits the purchase: deducts total cost from the player's balance,
   * adds the share to the player's portfolio, and archives the transaction.
   *
   * @param player the player making the purchase
   * @throws IllegalArgumentException if player is null
   * @throws IllegalStateException    if already committed or player has insufficient funds
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
    // Check the player can actually afford this purchase before changing any state.
    // getCalculator() returns the PurchaseCalculator, calculateTotal() returns gross + commission.
    // compareTo returns negative if player's money is less than the total cost.
    if (player.getMoney().compareTo(getCalculator().calculateTotal()) < 0) {
      throw new IllegalStateException("Insufficient funds to complete purchase");
    }

    // All checks passed - now execute the three steps of the purchase:

    // Step 1: Deduct the total cost from the player's cash balance
    player.withdrawMoney(getCalculator().calculateTotal());

    // Step 2: Add the share to the player's portfolio so they own it
    player.getPortfolio().addShare(getShare());

    // Step 3: Record this transaction in the player's archive.
    // 'this' refers to this Purchase object itself - it archives itself.
    player.getTransactionArchive().add(this);

    // Mark as committed so this transaction cannot be executed again
    committed = true;
  }
}
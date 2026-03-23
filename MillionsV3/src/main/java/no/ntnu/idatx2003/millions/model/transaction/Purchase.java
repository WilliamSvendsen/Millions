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
   *
   * @param share the share being purchased
   * @param week  the trading week number
   */
  public Purchase(Share share, int week) {
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
    if (committed) {
      throw new IllegalStateException("This transaction has already been committed");
    }
    if (player.getMoney().compareTo(getCalculator().calculateTotal()) < 0) {
      throw new IllegalStateException("Insufficient funds to complete purchase");
    }
    player.withdrawMoney(getCalculator().calculateTotal());
    player.getPortfolio().addShare(getShare());
    player.getTransactionArchive().add(this);
    committed = true;
  }
}

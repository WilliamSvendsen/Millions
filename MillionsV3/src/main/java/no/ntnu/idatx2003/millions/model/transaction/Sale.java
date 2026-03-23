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
   *
   * @param share the share being sold
   * @param week  the trading week number
   */
  public Sale(Share share, int week) {
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
    if (committed) {
      throw new IllegalStateException("This transaction has already been committed");
    }
    if (!player.getPortfolio().contains(getShare())) {
      throw new IllegalStateException("Player does not own the share being sold");
    }
    player.addMoney(getCalculator().calculateTotal());
    player.getPortfolio().removeShare(getShare());
    player.getTransactionArchive().add(this);
    committed = true;
  }
}

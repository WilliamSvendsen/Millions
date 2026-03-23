package no.ntnu.idatx2003.millions.model.transaction;

import no.ntnu.idatx2003.millions.model.Player;
import no.ntnu.idatx2003.millions.model.Share;
import no.ntnu.idatx2003.millions.model.calculator.TransactionCalculator;

/**
 * Abstract base class for all financial transactions.
 *
 * <p>Holds the share, week number, and calculator common to all transactions.
 * The {@code committed} flag is protected so subclasses can set it after a
 * successful commit. A transaction can only be committed once.</p>
 */
public abstract class Transaction {

  private final Share share;
  private final int week;
  private final TransactionCalculator calculator;

  /**
   * Flag indicating whether this transaction has been committed.
   * Set to true by subclasses after a successful commit.
   */
  protected boolean committed;

  /**
   * Creates a Transaction.
   *
   * @param share      the share involved in this transaction
   * @param week       the trading week number
   * @param calculator the calculator used to compute values
   * @throws IllegalArgumentException if share or calculator is null, or week is non-positive
   */
  protected Transaction(Share share, int week, TransactionCalculator calculator) {
    if (share == null) {
      throw new IllegalArgumentException("Share cannot be null");
    }
    if (week <= 0) {
      throw new IllegalArgumentException("Week must be positive");
    }
    if (calculator == null) {
      throw new IllegalArgumentException("Calculator cannot be null");
    }
    this.share = share;
    this.week = week;
    this.calculator = calculator;
    this.committed = false;
  }

  /**
   * Returns the share associated with this transaction.
   *
   * @return the share
   */
  public Share getShare() {
    return share;
  }

  /**
   * Returns the trading week this transaction belongs to.
   *
   * @return week number
   */
  public int getWeek() {
    return week;
  }

  /**
   * Returns the calculator for this transaction.
   *
   * @return transaction calculator
   */
  public TransactionCalculator getCalculator() {
    return calculator;
  }

  /**
   * Returns whether this transaction has already been committed.
   *
   * @return true if committed
   */
  public boolean isCommitted() {
    return committed;
  }

  /**
   * Commits this transaction for the given player.
   * A transaction that is already committed cannot be committed again.
   *
   * @param player the player executing the transaction
   */
  public abstract void commit(Player player);

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "{share=" + share
        + ", week=" + week
        + ", committed=" + committed
        + '}';
  }
}

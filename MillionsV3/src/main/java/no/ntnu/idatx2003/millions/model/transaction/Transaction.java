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

  // The share involved in this transaction - what stock and how many units
  private final Share share;

  // The trading week number when this transaction was created
  private final int week;

  // The calculator responsible for computing gross, commission, tax, and total.
  // Typed as the interface TransactionCalculator so this class does not need
  // to know whether it holds a PurchaseCalculator or SaleCalculator.
  private final TransactionCalculator calculator;

  // Tracks whether this transaction has been executed.
  // protected so that subclasses (Purchase and Sale) can set it to true
  // after a successful commit. private would prevent subclasses from accessing it.
  // Starts as false and can only ever move to true, never back.
  protected boolean committed;

  /**
   * Creates a Transaction.
   * This constructor is protected, it can only be called by subclasses
   * via super(), never directly from outside.
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
    // Week numbers must be 1 or higher - week 0 or negative makes no sense
    if (week <= 0) {
      throw new IllegalArgumentException("Week must be positive");
    }
    if (calculator == null) {
      throw new IllegalArgumentException("Calculator cannot be null");
    }
    this.share = share;
    this.week = week;
    this.calculator = calculator;
    // Every new transaction starts as uncommitted
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
   * Purchase returns a PurchaseCalculator, Sale returns a SaleCalculator,
   * but both are accessed through this common TransactionCalculator reference.
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
   * Declared abstract, each subclass must provide its own implementation since buying and selling work differently.
   * A transaction that is already committed cannot be committed again.
   *
   * @param player the player executing the transaction
   */
  public abstract void commit(Player player);

  // Produces a readable summary, e.g.
  // "Purchase{share=Share{stock=AAPL, ...}, week=1, committed=true}"
  // getClass().getSimpleName() returns "Purchase" or "Sale" depending on
  // which subclass this actually is at runtime
  @Override
  public String toString() {
    return getClass().getSimpleName()
            + "{share=" + share
            + ", week=" + week
            + ", committed=" + committed
            + '}';
  }
}
package no.ntnu.idatx2003.millions.model;

import java.math.BigDecimal;
import no.ntnu.idatx2003.millions.model.transaction.TransactionArchive;

/**
 * Represents a player in the Millions stock trading game.
 *
 * <p>A player has a name, a current money balance, a starting balance
 * (stored separately for future growth tracking), a portfolio of shares,
 * and a transaction archive.</p>
 */
public class Player {

  // The player's name, set once at creation and never changed
  private final String name;

  // The amount of money the player started with.
  // Stored permanently so we can later compare it to the current balance
  // to measure how much the player has grown (or lost) over time.
  private final BigDecimal startingMoney;

  // The player's current cash balance. This changes as they buy and sell stocks.
  // Not final because it must change - unlike startingMoney which is locked.
  private BigDecimal money;

  // The player's collection of currently owned shares
  private final Portfolio portfolio;

  // A record of every buy and sell the player has ever completed
  private final TransactionArchive transactionArchive;

  /**
   * Creates a new Player with the given name and starting capital.
   *
   * @param name          the player's name
   * @param startingMoney the initial cash balance (must be non-negative)
   * @throws IllegalArgumentException if name is blank or startingMoney is negative
   */
  public Player(String name, BigDecimal startingMoney) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Name cannot be null or blank");
    }
    // compareTo returns negative if startingMoney is less than zero
    if (startingMoney == null || startingMoney.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Starting money cannot be null or negative");
    }
    this.name = name;
    this.startingMoney = startingMoney;
    // At the start, current money equals starting money
    this.money = startingMoney;
    // Create a fresh empty portfolio, the player owns nothing yet
    this.portfolio = new Portfolio();
    // Create a fresh empty archive, no transactions yet
    this.transactionArchive = new TransactionArchive();
  }

  /**
   * Returns the player's name.
   *
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the player's current cash balance.
   *
   * @return current money
   */
  public BigDecimal getMoney() {
    return money;
  }

  /**
   * Returns the player's starting capital.
   * This never changes - useful for calculating growth over time.
   *
   * @return starting money
   */
  public BigDecimal getStartingMoney() {
    return startingMoney;
  }

  /**
   * Adds an amount to the player's cash balance.
   * Called by Sale.commit() when a sale completes successfully.
   *
   * @param amount the amount to add (must be positive)
   * @throws IllegalArgumentException if amount is null or not positive
   */
  public void addMoney(BigDecimal amount) {
    // compareTo returns <= 0 for zero or negative amounts, both of which are invalid
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }
    // add() is the BigDecimal equivalent of the + operator
    this.money = this.money.add(amount);
  }

  /**
   * Deducts an amount from the player's cash balance.
   * Called by Purchase.commit() when a purchase completes successfully.
   *
   * @param amount the amount to deduct (must be positive and not exceed balance)
   * @throws IllegalArgumentException if amount is null or not positive
   * @throws IllegalStateException    if the player has insufficient funds
   */
  public void withdrawMoney(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }
    // compareTo returns positive if amount is greater than current money balance
    if (amount.compareTo(this.money) > 0) {
      throw new IllegalStateException("Insufficient funds");
    }
    // subtract() is the BigDecimal equivalent of the - operator
    this.money = this.money.subtract(amount);
  }

  /**
   * Returns the player's portfolio.
   *
   * @return portfolio
   */
  public Portfolio getPortfolio() {
    return portfolio;
  }

  /**
   * Returns the player's transaction archive.
   *
   * @return transaction archive
   */
  public TransactionArchive getTransactionArchive() {
    return transactionArchive;
  }

  // Produces a readable summary, e.g. "Player{name='Alice', money=9500.00}"
  @Override
  public String toString() {
    return "Player{name='" + name + "', money=" + money + '}';
  }
}
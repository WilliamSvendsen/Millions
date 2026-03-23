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

  private final String name;
  private final BigDecimal startingMoney;
  private BigDecimal money;
  private final Portfolio portfolio;
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
    if (startingMoney == null || startingMoney.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Starting money cannot be null or negative");
    }
    this.name = name;
    this.startingMoney = startingMoney;
    this.money = startingMoney;
    this.portfolio = new Portfolio();
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
   *
   * @return starting money
   */
  public BigDecimal getStartingMoney() {
    return startingMoney;
  }

  /**
   * Adds an amount to the player's cash balance.
   *
   * @param amount the amount to add (must be positive)
   * @throws IllegalArgumentException if amount is null or not positive
   */
  public void addMoney(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }
    this.money = this.money.add(amount);
  }

  /**
   * Deducts an amount from the player's cash balance.
   *
   * @param amount the amount to deduct (must be positive and not exceed balance)
   * @throws IllegalArgumentException if amount is null or not positive
   * @throws IllegalStateException    if the player has insufficient funds
   */
  public void withdrawMoney(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }
    if (amount.compareTo(this.money) > 0) {
      throw new IllegalStateException("Insufficient funds");
    }
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

  @Override
  public String toString() {
    return "Player{name='" + name + "', money=" + money + '}';
  }
}

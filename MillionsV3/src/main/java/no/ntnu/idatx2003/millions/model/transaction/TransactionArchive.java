package no.ntnu.idatx2003.millions.model.transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stores the history of all committed transactions for a player.
 */
public class TransactionArchive {

  private final List<Transaction> transactions;

  /**
   * Creates a new empty TransactionArchive.
   */
  public TransactionArchive() {
    this.transactions = new ArrayList<>();
  }

  /**
   * Adds a committed transaction to the archive.
   *
   * @param transaction the transaction to store
   * @return true if successfully added
   * @throws IllegalArgumentException if transaction is null or not yet committed
   */
  public boolean add(Transaction transaction) {
    if (transaction == null) {
      throw new IllegalArgumentException("Transaction cannot be null");
    }
    if (!transaction.isCommitted()) {
      throw new IllegalArgumentException("Only committed transactions can be archived");
    }
    return transactions.add(transaction);
  }

  /**
   * Returns true if the archive contains no transactions.
   *
   * @return true if empty
   */
  public boolean isEmpty() {
    return transactions.isEmpty();
  }

  /**
   * Returns all transactions that occurred in the given week.
   *
   * @param week the week number to filter by
   * @return list of transactions in that week (may be empty)
   */
  public List<Transaction> getTransactions(int week) {
    return transactions.stream()
        .filter(t -> t.getWeek() == week)
        .collect(Collectors.toList());
  }

  /**
   * Counts the number of distinct weeks in which at least one transaction occurred.
   *
   * @return number of distinct trading weeks
   */
  public int countDistinctWeeks() {
    return (int) transactions.stream()
        .map(Transaction::getWeek)
        .distinct()
        .count();
  }

  /**
   * Returns a read-only view of all transactions.
   *
   * @return unmodifiable list of all transactions
   */
  public List<Transaction> getAllTransactions() {
    return Collections.unmodifiableList(transactions);
  }
}

package no.ntnu.idatx2003.millions.model.transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stores the history of all committed transactions for a player.
 * Provides ways to query transactions by week and count distinct trading weeks.
 */
public class TransactionArchive {

  // The internal list holding all committed transactions in the order they occurred
  private final List<Transaction> transactions;

  /**
   * Creates a new empty TransactionArchive.
   */
  public TransactionArchive() {
    this.transactions = new ArrayList<>();
  }

  /**
   * Adds a committed transaction to the archive.
   * Only committed transactions are accepted - this prevents incomplete
   * transactions from being recorded as if they happened.
   *
   * @param transaction the transaction to store
   * @return true if successfully added
   * @throws IllegalArgumentException if transaction is null or not yet committed
   */
  public boolean add(Transaction transaction) {
    if (transaction == null) {
      throw new IllegalArgumentException("Transaction cannot be null");
    }
    // isCommitted() returns the committed flag from the Transaction class.
    // The ! means "if NOT committed" - reject anything that hasn't been executed yet
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
    // stream() lets us process the list using filter and collect.
    // filter() keeps only transactions where the week number matches.
    // collect(Collectors.toList()) gathers the results into a new list.
    return transactions.stream()
            .filter(t -> t.getWeek() == week)
            .collect(Collectors.toList());
  }

  /**
   * Counts the number of distinct weeks in which at least one transaction occurred.
   * For example, if trades happened in weeks 1, 1, 3, and 5, this returns 3.
   * Used later to determine the player's status level.
   *
   * @return number of distinct trading weeks
   */
  public int countDistinctWeeks() {
    // map() extracts just the week number from each transaction.
    // distinct() removes duplicate week numbers.
    // count() counts how many unique weeks remain.
    // The result is cast to int since count() returns a long.
    return (int) transactions.stream()
            .map(Transaction::getWeek)
            .distinct()
            .count();
  }

  /**
   * Returns a read-only view of all transactions.
   * The caller can iterate and read, but cannot add or remove entries.
   *
   * @return unmodifiable list of all transactions
   */
  public List<Transaction> getAllTransactions() {
    return Collections.unmodifiableList(transactions);
  }
}
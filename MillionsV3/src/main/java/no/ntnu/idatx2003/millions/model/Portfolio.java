package no.ntnu.idatx2003.millions.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.math.BigDecimal;

/**
 * Stores all shares currently owned by a player.
 *
 * <p>Each purchase creates a distinct Share entry, even for the same stock.
 * This preserves the exact purchase price per batch, which is needed for
 * correct profit and tax calculations at sale time.</p>
 */
public class Portfolio {

  // The internal list holding all shares the player currently owns.
  // Each entry is a separate Share object from a single purchase.
  private final List<Share> shares;

  /**
   * Creates a new empty Portfolio.
   */
  public Portfolio() {
    // Start with an empty list - the player owns nothing yet
    this.shares = new ArrayList<>();
  }

  /**
   * Adds a share to the portfolio.
   *
   * @param share the share to add
   * @return true if successfully added
   * @throws IllegalArgumentException if share is null
   */
  public boolean addShare(Share share) {
    if (share == null) {
      throw new IllegalArgumentException("Share cannot be null");
    }
    // Delegates to the ArrayList's own add() method, which always returns true
    return shares.add(share);
  }

  /**
   * Removes a specific share from the portfolio.
   * Uses object identity - only removes the exact Share instance passed in.
   *
   * @param share the share to remove
   * @return true if the share was found and removed, false if it was not in the portfolio
   * @throws IllegalArgumentException if share is null
   */
  public boolean removeShare(Share share) {
    if (share == null) {
      throw new IllegalArgumentException("Share cannot be null");
    }
    // List.remove() returns true if the item was found and removed,
    // false if it was not present in the list
    return shares.remove(share);
  }

  /**
   * Returns all shares in the portfolio.
   *
   * @return unmodifiable list of all shares
   */
  public List<Share> getShares() {
    // unmodifiableList() wraps the list so the caller can read it
    // but cannot add or remove items directly.
    // The only way to change the portfolio is through addShare() and removeShare(),
    // both of which have validation.
    return Collections.unmodifiableList(shares);
  }

  /**
   * Returns all shares in the portfolio that belong to the given stock symbol.
   *
   * @param symbol the ticker symbol to filter by
   * @return list of matching shares (may be empty)
   * @throws IllegalArgumentException if symbol is null or blank
   */
  public List<Share> getShares(String symbol) {
    if (symbol == null || symbol.isBlank()) {
      throw new IllegalArgumentException("Symbol cannot be null or blank");
    }
    // stream() converts the list into a stream so we can filter it.
    // filter() keeps only the shares whose stock symbol matches the given symbol.
    // equalsIgnoreCase() means "AAPL" and "aapl" are treated as the same.
    // toList() collects the filtered results back into a new list.
    return shares.stream()
            .filter(s -> s.getStock().getSymbol().equalsIgnoreCase(symbol))
            .toList();
  }

  /**
   * Returns true if the portfolio contains the given share instance.
   * Checks by object reference - the share must be the exact same object.
   *
   * @param share the share to look for
   * @return true if found
   * @throws IllegalArgumentException if share is null
   */
  public boolean contains(Share share) {
    if (share == null) {
      throw new IllegalArgumentException("Share cannot be null");
    }
    // List.contains() checks whether this exact Share object exists in the list
    return shares.contains(share);
  }
  /**
   * Returns the total current market value of all shares in the portfolio.
   * Calculated by multiplying each share's current sales price by its quantity.
   *
   * @return total portfolio market value
   */
  public BigDecimal getNetWorth() {
    // stream() through all shares, multiply each share's current price by quantity,
    // then add all the results together starting from zero
    return shares.stream()
            .map(s -> s.getStock().getSalesPrice().multiply(s.getQuantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  // Produces a readable summary of the portfolio and all shares inside it
  @Override
  public String toString() {
    return "Portfolio{shares=" + shares + '}';
  }
}
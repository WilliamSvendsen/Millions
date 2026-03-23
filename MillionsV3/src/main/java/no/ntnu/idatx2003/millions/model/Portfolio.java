package no.ntnu.idatx2003.millions.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores all shares currently owned by a player.
 *
 * <p>Each purchase creates a distinct Share entry, even for the same stock.
 * This preserves the exact purchase price per batch, which is needed for
 * correct profit and tax calculations at sale time.</p>
 */
public class Portfolio {

  private final List<Share> shares;

  /**
   * Creates a new empty Portfolio.
   */
  public Portfolio() {
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
    return shares.add(share);
  }

  /**
   * Removes a specific share from the portfolio.
   *
   * @param share the share to remove
   * @return true if the share was found and removed
   * @throws IllegalArgumentException if share is null
   */
  public boolean removeShare(Share share) {
    if (share == null) {
      throw new IllegalArgumentException("Share cannot be null");
    }
    return shares.remove(share);
  }

  /**
   * Returns all shares in the portfolio.
   *
   * @return unmodifiable list of all shares
   */
  public List<Share> getShares() {
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
    return shares.stream()
        .filter(s -> s.getStock().getSymbol().equalsIgnoreCase(symbol))
        .toList();
  }

  /**
   * Returns true if the portfolio contains the given share instance.
   *
   * @param share the share to look for
   * @return true if found
   * @throws IllegalArgumentException if share is null
   */
  public boolean contains(Share share) {
    if (share == null) {
      throw new IllegalArgumentException("Share cannot be null");
    }
    return shares.contains(share);
  }

  @Override
  public String toString() {
    return "Portfolio{shares=" + shares + '}';
  }
}

package no.ntnu.idatx2003.millions.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PortfolioTest {

    private Portfolio portfolio;
    private Stock stock;
    private Share share;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio();
        stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("150.00"));
        share = new Share(stock, new BigDecimal("10"), new BigDecimal("150.00"));
    }

    @Test
    void newPortfolioIsEmpty() {
        assertTrue(portfolio.getShares().isEmpty());
    }

    @Test
    void addShareIncreasesPortfolioSize() {
        portfolio.addShare(share);
        assertEquals(1, portfolio.getShares().size());
    }

    @Test
    void addShareThrowsOnNull() {
        assertThrows(IllegalArgumentException.class, () -> portfolio.addShare(null));
    }

    @Test
    void removeShareDecreasesPortfolioSize() {
        portfolio.addShare(share);
        portfolio.removeShare(share);
        assertTrue(portfolio.getShares().isEmpty());
    }

    @Test
    void removeShareReturnsFalseWhenNotInPortfolio() {
        assertFalse(portfolio.removeShare(share));
    }

    @Test
    void removeShareThrowsOnNull() {
        assertThrows(IllegalArgumentException.class, () -> portfolio.removeShare(null));
    }

    @Test
    void containsReturnsTrueForAddedShare() {
        portfolio.addShare(share);
        assertTrue(portfolio.contains(share));
    }

    @Test
    void containsReturnsFalseForShareNotInPortfolio() {
        assertFalse(portfolio.contains(share));
    }

    @Test
    void containsThrowsOnNull() {
        assertThrows(IllegalArgumentException.class, () -> portfolio.contains(null));
    }

    @Test
    void getSharesBySymbolReturnsCorrectShares() {
        portfolio.addShare(share);
        List<Share> result = portfolio.getShares("AAPL");
        assertEquals(1, result.size());
        assertEquals(share, result.get(0));
    }

    @Test
    void getSharesBySymbolReturnsEmptyForUnknownSymbol() {
        portfolio.addShare(share);
        assertTrue(portfolio.getShares("TSLA").isEmpty());
    }

    @Test
    void getNetWorthCalculatesCorrectly() {
        portfolio.addShare(share);
        // 10 units at current price 150.00 = 1500.00
        assertEquals(new BigDecimal("1500.00"), portfolio.getNetWorth());
    }

    @Test
    void getNetWorthIsZeroForEmptyPortfolio() {
        assertEquals(BigDecimal.ZERO, portfolio.getNetWorth());
    }

    @Test
    void getSharesListIsUnmodifiable() {
        portfolio.addShare(share);
        assertThrows(UnsupportedOperationException.class,
                () -> portfolio.getShares().add(share));
    }
}
package no.ntnu.idatx2003.millions.model.transaction;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import no.ntnu.idatx2003.millions.model.Player;
import no.ntnu.idatx2003.millions.model.Share;
import no.ntnu.idatx2003.millions.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SaleTest {

    private Player player;
    private Stock stock;
    private Share share;

    @BeforeEach
    void setUp() {
        player = new Player("Alice", new BigDecimal("10000.00"));
        stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("100.00"));
        share = new Share(stock, new BigDecimal("5"), new BigDecimal("100.00"));
        // Legg til andelen i porteføljen slik at den kan selges
        player.getPortfolio().addShare(share);
    }

    @Test
    void commitRemovesShareFromPortfolio() {
        Sale sale = new Sale(share, 1);
        sale.commit(player);
        assertFalse(player.getPortfolio().contains(share));
    }

    @Test
    void commitAddsMoneyToPlayer() {
        Sale sale = new Sale(share, 1);
        BigDecimal before = player.getMoney();
        sale.commit(player);
        assertTrue(player.getMoney().compareTo(before) > 0);
    }

    @Test
    void commitArchivesTransaction() {
        Sale sale = new Sale(share, 1);
        sale.commit(player);
        assertFalse(player.getTransactionArchive().isEmpty());
    }

    @Test
    void commitSetsCommittedFlag() {
        Sale sale = new Sale(share, 1);
        sale.commit(player);
        assertTrue(sale.isCommitted());
    }

    @Test
    void commitThrowsWhenAlreadyCommitted() {
        Sale sale = new Sale(share, 1);
        sale.commit(player);
        assertThrows(IllegalStateException.class, () -> sale.commit(player));
    }

    @Test
    void commitThrowsWhenPlayerDoesNotOwnShare() {
        Player otherPlayer = new Player("Bob", new BigDecimal("10000.00"));
        Sale sale = new Sale(share, 1);
        assertThrows(IllegalStateException.class, () -> sale.commit(otherPlayer));
    }

    @Test
    void commitThrowsOnNullPlayer() {
        Sale sale = new Sale(share, 1);
        assertThrows(IllegalArgumentException.class, () -> sale.commit(null));
    }
}
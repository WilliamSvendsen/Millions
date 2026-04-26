package no.ntnu.idatx2003.millions.model.transaction;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import no.ntnu.idatx2003.millions.model.Player;
import no.ntnu.idatx2003.millions.model.Share;
import no.ntnu.idatx2003.millions.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PurchaseTest {

    private Player player;
    private Stock stock;
    private Share share;

    @BeforeEach
    void setUp() {
        player = new Player("Alice", new BigDecimal("10000.00"));
        stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("100.00"));
        share = new Share(stock, new BigDecimal("5"), new BigDecimal("100.00"));
    }

    @Test
    void commitAddsShareToPortfolio() {
        Purchase purchase = new Purchase(share, 1);
        purchase.commit(player);
        assertTrue(player.getPortfolio().contains(share));
    }

    @Test
    void commitDeductsMoneyFromPlayer() {
        Purchase purchase = new Purchase(share, 1);
        BigDecimal before = player.getMoney();
        purchase.commit(player);
        assertTrue(player.getMoney().compareTo(before) < 0);
    }

    @Test
    void commitArchivesTransaction() {
        Purchase purchase = new Purchase(share, 1);
        purchase.commit(player);
        assertFalse(player.getTransactionArchive().isEmpty());
    }

    @Test
    void commitSetsCommittedFlag() {
        Purchase purchase = new Purchase(share, 1);
        purchase.commit(player);
        assertTrue(purchase.isCommitted());
    }

    @Test
    void commitThrowsWhenAlreadyCommitted() {
        Purchase purchase = new Purchase(share, 1);
        purchase.commit(player);
        assertThrows(IllegalStateException.class, () -> purchase.commit(player));
    }

    @Test
    void commitThrowsWhenInsufficientFunds() {
        Player poorPlayer = new Player("Bob", new BigDecimal("1.00"));
        Purchase purchase = new Purchase(share, 1);
        assertThrows(IllegalStateException.class, () -> purchase.commit(poorPlayer));
    }

    @Test
    void commitThrowsOnNullPlayer() {
        Purchase purchase = new Purchase(share, 1);
        assertThrows(IllegalArgumentException.class, () -> purchase.commit(null));
    }
}
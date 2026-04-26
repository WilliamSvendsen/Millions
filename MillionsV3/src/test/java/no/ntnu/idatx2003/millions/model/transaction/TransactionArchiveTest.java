package no.ntnu.idatx2003.millions.model.transaction;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import no.ntnu.idatx2003.millions.model.Player;
import no.ntnu.idatx2003.millions.model.Share;
import no.ntnu.idatx2003.millions.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionArchiveTest {

    private TransactionArchive archive;
    private Player player;
    private Stock stock;
    private Share share;

    @BeforeEach
    void setUp() {
        archive = new TransactionArchive();
        player = new Player("Alice", new BigDecimal("10000.00"));
        stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("100.00"));
        share = new Share(stock, new BigDecimal("5"), new BigDecimal("100.00"));
    }

    @Test
    void newArchiveIsEmpty() {
        assertTrue(archive.isEmpty());
    }

    @Test
    void addCommittedTransactionSucceeds() {
        Purchase purchase = new Purchase(share, 1);
        purchase.commit(player);
        assertFalse(player.getTransactionArchive().isEmpty());
    }

    @Test
    void addThrowsOnNull() {
        assertThrows(IllegalArgumentException.class, () -> archive.add(null));
    }

    @Test
    void addThrowsOnUncommittedTransaction() {
        Purchase purchase = new Purchase(share, 1);
        assertThrows(IllegalArgumentException.class, () -> archive.add(purchase));
    }

    @Test
    void getTransactionsByWeekReturnsCorrectTransactions() {
        Purchase purchase = new Purchase(share, 1);
        purchase.commit(player);
        List<Transaction> result = player.getTransactionArchive().getTransactions(1);
        assertEquals(1, result.size());
    }

    @Test
    void getTransactionsByWeekReturnsEmptyForUnknownWeek() {
        Purchase purchase = new Purchase(share, 1);
        purchase.commit(player);
        List<Transaction> result = player.getTransactionArchive().getTransactions(99);
        assertTrue(result.isEmpty());
    }

    @Test
    void countDistinctWeeksCountsCorrectly() {
        Purchase purchase1 = new Purchase(share, 1);
        purchase1.commit(player);

        Share share2 = new Share(stock, new BigDecimal("2"), new BigDecimal("100.00"));
        Purchase purchase2 = new Purchase(share2, 1);
        purchase2.commit(player);

        Share share3 = new Share(stock, new BigDecimal("1"), new BigDecimal("100.00"));
        Purchase purchase3 = new Purchase(share3, 3);
        purchase3.commit(player);

        assertEquals(2, player.getTransactionArchive().countDistinctWeeks());
    }

    @Test
    void getAllTransactionsIsUnmodifiable() {
        Purchase purchase = new Purchase(share, 1);
        purchase.commit(player);
        assertThrows(UnsupportedOperationException.class,
                () -> player.getTransactionArchive().getAllTransactions().clear());
    }
}
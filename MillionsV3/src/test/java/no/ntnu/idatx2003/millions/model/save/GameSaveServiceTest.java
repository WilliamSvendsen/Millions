package no.ntnu.idatx2003.millions.model.save;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import no.ntnu.idatx2003.millions.model.Exchange;
import no.ntnu.idatx2003.millions.model.Player;
import no.ntnu.idatx2003.millions.model.Share;
import no.ntnu.idatx2003.millions.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GameSaveServiceTest {

    private GameSaveService saveService;
    private Player player;
    private Exchange exchange;

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() {
        saveService = new GameSaveService();
        player = new Player("Alice", new BigDecimal("10000.00"));
        Stock stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("150.00"));
        exchange = new Exchange("Test Exchange", List.of(stock));
    }

    @Test
    void saveAndLoadRestoresPlayerName() throws IOException {
        File file = new File(tempDir, "save.json");
        saveService.saveGame(player, exchange, file.getAbsolutePath());
        GameState state = saveService.loadGame(file.getAbsolutePath());
        assertEquals("Alice", state.getPlayer().getName());
    }

    @Test
    void saveAndLoadRestoresPlayerMoney() throws IOException {
        File file = new File(tempDir, "save.json");
        player.withdrawMoney(new BigDecimal("1000.00"));
        saveService.saveGame(player, exchange, file.getAbsolutePath());
        GameState state = saveService.loadGame(file.getAbsolutePath());
        assertEquals(new BigDecimal("9000.00"), state.getPlayer().getMoney());
    }

    @Test
    void saveAndLoadRestoresWeekNumber() throws IOException {
        File file = new File(tempDir, "save.json");
        exchange.advance();
        exchange.advance();
        saveService.saveGame(player, exchange, file.getAbsolutePath());
        GameState state = saveService.loadGame(file.getAbsolutePath());
        assertEquals(3, state.getExchange().getWeek());
    }

    @Test
    void saveAndLoadRestoresStocks() throws IOException {
        File file = new File(tempDir, "save.json");
        saveService.saveGame(player, exchange, file.getAbsolutePath());
        GameState state = saveService.loadGame(file.getAbsolutePath());
        assertTrue(state.getExchange().hasStock("AAPL"));
    }

    @Test
    void saveAndLoadRestoresPortfolio() throws IOException {
        File file = new File(tempDir, "save.json");
        Stock stock = exchange.getStock("AAPL");
        Share share = new Share(stock, new BigDecimal("5"), new BigDecimal("150.00"));
        player.getPortfolio().addShare(share);
        saveService.saveGame(player, exchange, file.getAbsolutePath());
        GameState state = saveService.loadGame(file.getAbsolutePath());
        assertFalse(state.getPlayer().getPortfolio().getShares().isEmpty());
    }

    @Test
    void saveAndLoadRestoresPriceHistory() throws IOException {
        File file = new File(tempDir, "save.json");
        exchange.advance();
        exchange.advance();
        int expectedPrices = exchange.getStock("AAPL").getHistoricalPrices().size();
        saveService.saveGame(player, exchange, file.getAbsolutePath());
        GameState state = saveService.loadGame(file.getAbsolutePath());
        assertEquals(expectedPrices, state.getExchange().getStock("AAPL")
                .getHistoricalPrices().size());
    }

    @Test
    void loadThrowsOnNonExistentFile() {
        assertThrows(IOException.class,
                () -> saveService.loadGame("nonexistent/save.json"));
    }
}
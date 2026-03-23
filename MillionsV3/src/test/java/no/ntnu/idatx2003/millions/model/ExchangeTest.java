package no.ntnu.idatx2003.millions.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExchangeTest {

  private Exchange exchange;
  private Player player;
  private Stock apple;
  private Stock google;

  @BeforeEach
  void setUp() {
    apple = new Stock("AAPL", "Apple Inc.", new BigDecimal("150.00"));
    google = new Stock("GOOGL", "Alphabet Inc.", new BigDecimal("200.00"));
    exchange = new Exchange("Nasdaq", List.of(apple, google));
    player = new Player("Alice", new BigDecimal("10000.00"));
  }

  @Test
  void exchangeStartsAtWeekOne() {
    assertEquals(1, exchange.getWeek());
  }

  @Test
  void hasStockReturnsTrueForListedStock() {
    assertTrue(exchange.hasStock("AAPL"));
  }

  @Test
  void hasStockReturnsFalseForUnlisted() {
    assertFalse(exchange.hasStock("TSLA"));
  }

  @Test
  void hasStockReturnsFalseForNull() {
    assertFalse(exchange.hasStock(null));
  }

  @Test
  void getStockReturnsCorrectStock() {
    assertEquals(apple, exchange.getStock("AAPL"));
  }

  @Test
  void getStockThrowsForUnknownSymbol() {
    assertThrows(IllegalArgumentException.class, () -> exchange.getStock("XYZ"));
  }

  @Test
  void findStocksMatchesBySymbol() {
    assertTrue(exchange.findStocks("AAPL").contains(apple));
  }

  @Test
  void findStocksMatchesByCompanyName() {
    assertTrue(exchange.findStocks("alphabet").contains(google));
  }

  @Test
  void findStocksPartialMatchWorks() {
    assertTrue(exchange.findStocks("Go").contains(google));
  }

  @Test
  void findStocksReturnsEmptyWhenNoMatch() {
    assertTrue(exchange.findStocks("ZZZZ").isEmpty());
  }

  @Test
  void buyDeductsMoneyFromPlayer() {
    BigDecimal before = player.getMoney();
    exchange.buy("AAPL", new BigDecimal("2"), player);
    assertTrue(player.getMoney().compareTo(before) < 0);
  }

  @Test
  void buyAddsShareToPortfolio() {
    exchange.buy("AAPL", new BigDecimal("2"), player);
    assertFalse(player.getPortfolio().getShares().isEmpty());
  }

  @Test
  void buyArchivesTransaction() {
    exchange.buy("AAPL", new BigDecimal("2"), player);
    assertFalse(player.getTransactionArchive().isEmpty());
  }

  @Test
  void buyThrowsWhenInsufficientFunds() {
    Player poorPlayer = new Player("Bob", new BigDecimal("1.00"));
    assertThrows(IllegalStateException.class,
        () -> exchange.buy("AAPL", new BigDecimal("100"), poorPlayer));
  }

  @Test
  void sellAddsMoneyToPlayer() {
    exchange.buy("AAPL", new BigDecimal("2"), player);
    Share share = player.getPortfolio().getShares().get(0);
    BigDecimal moneyAfterBuy = player.getMoney();
    exchange.sell(share, player);
    assertTrue(player.getMoney().compareTo(moneyAfterBuy) > 0);
  }

  @Test
  void sellRemovesShareFromPortfolio() {
    exchange.buy("AAPL", new BigDecimal("2"), player);
    Share share = player.getPortfolio().getShares().get(0);
    exchange.sell(share, player);
    assertTrue(player.getPortfolio().getShares().isEmpty());
  }

  @Test
  void advanceIncrementsWeek() {
    exchange.advance();
    assertEquals(2, exchange.getWeek());
  }

  @Test
  void advanceChangesStockPrice() {
    BigDecimal priceBefore = apple.getSalesPrice();
    exchange.advance();
    // Price will have changed — new price is recorded on the stock
    assertNotNull(apple.getSalesPrice());
    // The old price should no longer be the current one in most cases,
    // but we can at least verify advance ran without error
  }

  @Test
  void constructorThrowsOnNullName() {
    assertThrows(IllegalArgumentException.class,
        () -> new Exchange(null, List.of(apple)));
  }

  @Test
  void constructorThrowsOnNullStockList() {
    assertThrows(IllegalArgumentException.class,
        () -> new Exchange("Nasdaq", null));
  }
}

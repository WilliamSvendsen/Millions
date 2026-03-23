package no.ntnu.idatx2003.millions.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlayerTest {

  private Player player;

  @BeforeEach
  void setUp() {
    player = new Player("Alice", new BigDecimal("10000.00"));
  }

  @Test
  void constructorSetsNameAndMoney() {
    assertEquals("Alice", player.getName());
    assertEquals(new BigDecimal("10000.00"), player.getMoney());
  }

  @Test
  void startingMoneyIsStoredSeparately() {
    player.withdrawMoney(new BigDecimal("500.00"));
    assertEquals(new BigDecimal("10000.00"), player.getStartingMoney());
    assertEquals(new BigDecimal("9500.00"), player.getMoney());
  }

  @Test
  void constructorThrowsOnBlankName() {
    assertThrows(IllegalArgumentException.class,
        () -> new Player("  ", new BigDecimal("1000")));
  }

  @Test
  void constructorThrowsOnNegativeMoney() {
    assertThrows(IllegalArgumentException.class,
        () -> new Player("Bob", new BigDecimal("-1")));
  }

  @Test
  void addMoneyIncreasesBalance() {
    player.addMoney(new BigDecimal("500.00"));
    assertEquals(new BigDecimal("10500.00"), player.getMoney());
  }

  @Test
  void addMoneyThrowsOnZeroAmount() {
    assertThrows(IllegalArgumentException.class,
        () -> player.addMoney(BigDecimal.ZERO));
  }

  @Test
  void withdrawMoneyDecreasesBalance() {
    player.withdrawMoney(new BigDecimal("1000.00"));
    assertEquals(new BigDecimal("9000.00"), player.getMoney());
  }

  @Test
  void withdrawMoneyThrowsWhenInsufficientFunds() {
    assertThrows(IllegalStateException.class,
        () -> player.withdrawMoney(new BigDecimal("99999.00")));
  }

  @Test
  void portfolioAndArchiveStartEmpty() {
    assertTrue(player.getPortfolio().getShares().isEmpty());
    assertTrue(player.getTransactionArchive().isEmpty());
  }
}

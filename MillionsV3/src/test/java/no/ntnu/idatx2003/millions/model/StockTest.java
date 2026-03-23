package no.ntnu.idatx2003.millions.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StockTest {

  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("150.00"));
  }

  @Test
  void constructorSetsFieldsCorrectly() {
    assertEquals("AAPL", stock.getSymbol());
    assertEquals("Apple Inc.", stock.getCompany());
    assertEquals(new BigDecimal("150.00"), stock.getSalesPrice());
  }

  @Test
  void constructorUppercasesSymbol() {
    Stock s = new Stock("aapl", "Apple Inc.", new BigDecimal("100"));
    assertEquals("AAPL", s.getSymbol());
  }

  @Test
  void constructorThrowsOnBlankSymbol() {
    assertThrows(IllegalArgumentException.class,
        () -> new Stock("  ", "Apple Inc.", new BigDecimal("100")));
  }

  @Test
  void constructorThrowsOnNullCompany() {
    assertThrows(IllegalArgumentException.class,
        () -> new Stock("AAPL", null, new BigDecimal("100")));
  }

  @Test
  void constructorThrowsOnNegativePrice() {
    assertThrows(IllegalArgumentException.class,
        () -> new Stock("AAPL", "Apple Inc.", new BigDecimal("-1")));
  }

  @Test
  void addNewSalesPriceUpdatesCurrentPrice() {
    stock.addNewSalesPrice(new BigDecimal("200.00"));
    assertEquals(new BigDecimal("200.00"), stock.getSalesPrice());
  }

  @Test
  void addNewSalesPriceThrowsOnNegative() {
    assertThrows(IllegalArgumentException.class,
        () -> stock.addNewSalesPrice(new BigDecimal("-5")));
  }

  @Test
  void equalsBasedOnSymbol() {
    Stock other = new Stock("AAPL", "Different Name", new BigDecimal("999"));
    assertEquals(stock, other);
  }

  @Test
  void stocksWithDifferentSymbolsAreNotEqual() {
    Stock other = new Stock("MSFT", "Microsoft", new BigDecimal("150.00"));
    assertNotEquals(stock, other);
  }
}

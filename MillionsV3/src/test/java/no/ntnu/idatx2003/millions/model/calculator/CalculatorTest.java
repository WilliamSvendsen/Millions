package no.ntnu.idatx2003.millions.model.calculator;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import no.ntnu.idatx2003.millions.model.Share;
import no.ntnu.idatx2003.millions.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CalculatorTest {

  private Stock stock;
  private Share share;

  @BeforeEach
  void setUp() {
    // Purchased at 100, currently trading at 200
    stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("100.00"));
    stock.addNewSalesPrice(new BigDecimal("200.00"));
    share = new Share(stock, new BigDecimal("10"), new BigDecimal("100.00"));
  }

  // --- PurchaseCalculator ---

  @Test
  void purchaseGrossIsPurchasePriceTimesQuantity() {
    PurchaseCalculator calc = new PurchaseCalculator(share);
    assertEquals(new BigDecimal("1000.00"), calc.calculateGross());
  }

  @Test
  void purchaseCommissionIsHalfPercent() {
    PurchaseCalculator calc = new PurchaseCalculator(share);
    assertEquals(new BigDecimal("5.00"), calc.calculateCommission());
  }

  @Test
  void purchaseTaxIsZero() {
    PurchaseCalculator calc = new PurchaseCalculator(share);
    assertEquals(new BigDecimal("0.00"), calc.calculateTax());
  }

  @Test
  void purchaseTotalIsGrossPlusCommission() {
    PurchaseCalculator calc = new PurchaseCalculator(share);
    assertEquals(new BigDecimal("1005.00"), calc.calculateTotal());
  }

  @Test
  void purchaseCalculatorThrowsOnNullShare() {
    assertThrows(IllegalArgumentException.class, () -> new PurchaseCalculator(null));
  }

  // --- SaleCalculator ---

  @Test
  void saleGrossIsSalesPriceTimesQuantity() {
    SaleCalculator calc = new SaleCalculator(share);
    assertEquals(new BigDecimal("2000.00"), calc.calculateGross());
  }

  @Test
  void saleCommissionIsOnePercent() {
    SaleCalculator calc = new SaleCalculator(share);
    assertEquals(new BigDecimal("20.00"), calc.calculateCommission());
  }

  @Test
  void saleTaxIsThirtyPercentOfGain() {
    SaleCalculator calc = new SaleCalculator(share);
    // gross=2000, commission=20, purchaseCost=1000, gain=980, tax=294
    assertEquals(new BigDecimal("294.00"), calc.calculateTax());
  }

  @Test
  void saleNoTaxWhenSoldAtLoss() {
    Stock lossStock = new Stock("TST", "Test Co", new BigDecimal("100.00"));
    lossStock.addNewSalesPrice(new BigDecimal("50.00"));
    Share lossShare = new Share(lossStock, new BigDecimal("10"), new BigDecimal("100.00"));
    SaleCalculator calc = new SaleCalculator(lossShare);
    assertEquals(new BigDecimal("0.00"), calc.calculateTax());
  }

  @Test
  void saleTotalIsGrossMinusCommissionMinusTax() {
    SaleCalculator calc = new SaleCalculator(share);
    // 2000 - 20 - 294 = 1686
    assertEquals(new BigDecimal("1686.00"), calc.calculateTotal());
  }

  @Test
  void saleCalculatorThrowsOnNullShare() {
    assertThrows(IllegalArgumentException.class, () -> new SaleCalculator(null));
  }
}

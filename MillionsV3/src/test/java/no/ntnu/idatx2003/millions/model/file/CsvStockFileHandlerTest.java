package no.ntnu.idatx2003.millions.model.file;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import no.ntnu.idatx2003.millions.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvStockFileHandlerTest {

    private CsvStockFileHandler handler;

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() {
        handler = new CsvStockFileHandler();
    }

    @Test
    void readStocksReturnsCorrectStocks() throws IOException {
        File file = new File(tempDir, "stocks.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("AAPL,Apple Inc.,150.00\n");
            writer.write("MSFT,Microsoft,400.00\n");
        }
        List<Stock> stocks = handler.readStocks(file.getAbsolutePath());
        assertEquals(2, stocks.size());
    }

    @Test
    void readStocksSkipsCommentLines() throws IOException {
        File file = new File(tempDir, "stocks.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("# This is a comment\n");
            writer.write("AAPL,Apple Inc.,150.00\n");
        }
        List<Stock> stocks = handler.readStocks(file.getAbsolutePath());
        assertEquals(1, stocks.size());
    }

    @Test
    void readStocksSkipsBlankLines() throws IOException {
        File file = new File(tempDir, "stocks.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("\n");
            writer.write("AAPL,Apple Inc.,150.00\n");
            writer.write("\n");
        }
        List<Stock> stocks = handler.readStocks(file.getAbsolutePath());
        assertEquals(1, stocks.size());
    }

    @Test
    void readStocksSkipsMalformedLines() throws IOException {
        File file = new File(tempDir, "stocks.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("AAPL,Apple Inc.,150.00\n");
            writer.write("INVALID_LINE\n");
        }
        List<Stock> stocks = handler.readStocks(file.getAbsolutePath());
        assertEquals(1, stocks.size());
    }

    @Test
    void readStocksSkipsInvalidPrice() throws IOException {
        File file = new File(tempDir, "stocks.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("AAPL,Apple Inc.,NOTANUMBER\n");
        }
        List<Stock> stocks = handler.readStocks(file.getAbsolutePath());
        assertEquals(0, stocks.size());
    }

    @Test
    void readStocksParsesSymbolCorrectly() throws IOException {
        File file = new File(tempDir, "stocks.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("AAPL,Apple Inc.,150.00\n");
        }
        List<Stock> stocks = handler.readStocks(file.getAbsolutePath());
        assertEquals("AAPL", stocks.get(0).getSymbol());
    }

    @Test
    void readStocksParsesPriceCorrectly() throws IOException {
        File file = new File(tempDir, "stocks.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("AAPL,Apple Inc.,150.00\n");
        }
        List<Stock> stocks = handler.readStocks(file.getAbsolutePath());
        assertEquals(new BigDecimal("150.00"), stocks.get(0).getSalesPrice());
    }

    @Test
    void readStocksThrowsOnNonExistentFile() {
        assertThrows(IOException.class,
                () -> handler.readStocks("nonexistent/file.csv"));
    }

    @Test
    void writeStocksCreatesReadableFile() throws IOException {
        File file = new File(tempDir, "output.csv");
        List<Stock> stocks = List.of(
                new Stock("AAPL", "Apple Inc.", new BigDecimal("150.00")),
                new Stock("MSFT", "Microsoft", new BigDecimal("400.00"))
        );
        handler.writeStocks(stocks, file.getAbsolutePath());
        List<Stock> loaded = handler.readStocks(file.getAbsolutePath());
        assertEquals(2, loaded.size());
    }

    @Test
    void writeStocksThrowsOnNullList() {
        File file = new File(tempDir, "output.csv");
        assertThrows(IllegalArgumentException.class,
                () -> handler.writeStocks(null, file.getAbsolutePath()));
    }
}
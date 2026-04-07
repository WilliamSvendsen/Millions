package no.ntnu.idatx2003.millions.model.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import no.ntnu.idatx2003.millions.model.Stock;

/**
 * Reads and writes stock data in CSV format.
 *
 * <p>Expected file format per line: symbol,name,price
 * Lines starting with # are treated as comments and skipped.
 * Blank lines are also skipped.</p>
 */
public class CsvStockFileHandler implements StockFileHandler {

    // The character used to separate values on each line
    private static final String DELIMITER = ",";

    // Lines starting with this character are comments and should be ignored
    private static final String COMMENT_PREFIX = "#";

    // Each stock line must have exactly this many fields: symbol, name, price
    private static final int EXPECTED_FIELDS = 3;

    /**
     * Reads stocks from a CSV file at the given path.
     * Skips blank lines and lines starting with #.
     * Skips lines that are malformed rather than crashing.
     *
     * @param filePath path to the CSV file
     * @return list of stocks parsed from the file
     * @throws IOException if the file cannot be opened or read
     */
    @Override
    public List<Stock> readStocks(String filePath) throws IOException {
        List<Stock> stocks = new ArrayList<>();

        // try-with-resources ensures the file is always closed after reading
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            // readLine() returns null when there are no more lines in the file
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip blank lines
                if (line.isEmpty()) {
                    continue;
                }

                // Skip comment lines starting with #
                if (line.startsWith(COMMENT_PREFIX)) {
                    continue;
                }

                // Try to parse this line - skip it if malformed
                Stock stock = parseLine(line);
                if (stock != null) {
                    stocks.add(stock);
                }
            }
        }
        return stocks;
    }

    /**
     * Writes stocks to a CSV file at the given path.
     * Overwrites the file if it already exists.
     *
     * @param stocks   the stocks to write
     * @param filePath path to the CSV file to write to
     * @throws IOException if the file cannot be written
     */
    @Override
    public void writeStocks(List<Stock> stocks, String filePath) throws IOException {
        if (stocks == null) {
            throw new IllegalArgumentException("Stocks list cannot be null");
        }

        // try-with-resources ensures the file is always closed after writing
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("# Ticker,Name,Price");
            writer.newLine();

            for (Stock stock : stocks) {
                writer.write(stock.getSymbol()
                        + DELIMITER + stock.getCompany()
                        + DELIMITER + stock.getSalesPrice());
                writer.newLine();
            }
        }
    }

    /**
     * Attempts to parse a single CSV line into a Stock object.
     * Returns null if the line is malformed.
     *
     * @param line a single non-blank, non-comment line from the file
     * @return a Stock object, or null if the line could not be parsed
     */
    private Stock parseLine(String line) {
        String[] fields = line.split(DELIMITER);

        if (fields.length != EXPECTED_FIELDS) {
            System.err.println("Skipping malformed line (wrong number of fields): " + line);
            return null;
        }

        String symbol = fields[0].trim();
        String name = fields[1].trim();
        String priceText = fields[2].trim();

        try {
            BigDecimal price = new BigDecimal(priceText);
            return new Stock(symbol, name, price);
        } catch (NumberFormatException e) {
            System.err.println("Skipping line with invalid price: " + line);
            return null;
        } catch (IllegalArgumentException e) {
            System.err.println("Skipping line with invalid stock data: " + line);
            return null;
        }
    }
}
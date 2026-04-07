package no.ntnu.idatx2003.millions.model.file;

import java.io.IOException;
import java.util.List;
import no.ntnu.idatx2003.millions.model.Stock;

/**
 * Interface for reading and writing stock data to and from files.
 *
 * <p>Implementing this interface for each file format (e.g. CSV, JSON)
 * means Exchange never needs to change when new formats are added —
 * it always works through this common contract.</p>
 */
public interface StockFileHandler {

    /**
     * Reads a list of stocks from the given file path.
     *
     * @param filePath path to the file to read from
     * @return list of stocks parsed from the file
     * @throws IOException if the file cannot be read
     */
    List<Stock> readStocks(String filePath) throws IOException;

    /**
     * Writes a list of stocks to the given file path.
     *
     * @param stocks   the stocks to write
     * @param filePath path to the file to write to
     * @throws IOException if the file cannot be written
     */
    void writeStocks(List<Stock> stocks, String filePath) throws IOException;
}
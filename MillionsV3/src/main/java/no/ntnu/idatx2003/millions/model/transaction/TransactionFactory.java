package no.ntnu.idatx2003.millions.model.transaction;

import no.ntnu.idatx2003.millions.model.Share;

/**
 * Factory for creating financial transactions.
 *
 * <p>Centralizes the creation of Purchase and Sale objects so the rest
 * of the code never needs to instantiate them directly. This makes it
 * easy to change how transactions are created in one place.</p>
 */
public class TransactionFactory {

    /**
     * Creates a new Purchase transaction for the given share and week.
     *
     * @param share the share being purchased
     * @param week  the current trading week
     * @return a new uncommitted Purchase transaction
     */
    public Purchase createPurchase(Share share, int week) {
        return new Purchase(share, week);
    }

    /**
     * Creates a new Sale transaction for the given share and week.
     *
     * @param share the share being sold
     * @param week  the current trading week
     * @return a new uncommitted Sale transaction
     */
    public Sale createSale(Share share, int week) {
        return new Sale(share, week);
    }
}
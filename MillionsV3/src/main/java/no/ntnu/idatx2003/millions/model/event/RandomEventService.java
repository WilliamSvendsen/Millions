package no.ntnu.idatx2003.millions.model.event;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import no.ntnu.idatx2003.millions.model.Stock;

/**
 * Service that generates random market events affecting stock prices.
 * Each week there is a chance that one or more stocks are hit by a
 * significant random event, causing a larger than normal price change.
 */
public class RandomEventService {

    // Chance that any single stock gets hit by an event each week (10%)
    private static final double EVENT_CHANCE = 0.10;

    // Possible event descriptions for positive events
    private static final String[] POSITIVE_EVENTS = {
            "Strong earnings report",
            "Major government contract won",
            "Breakthrough product announcement",
            "Analyst upgrades to strong buy",
            "Record quarterly revenue"
    };

    // Possible event descriptions for negative events
    private static final String[] NEGATIVE_EVENTS = {
            "Disappointing earnings report",
            "CEO resignation announced",
            "Product recall issued",
            "Regulatory investigation launched",
            "Major data breach reported"
    };

    private final Random random;

    /**
     * Creates a new RandomEventService.
     */
    public RandomEventService() {
        this.random = new Random();
    }

    /**
     * Processes random events for all stocks.
     * Each stock has a 10% chance of being affected by an event.
     * Returns a list of event descriptions for display in the GUI.
     *
     * @param stocks the list of stocks to process events for
     * @return list of event messages that occurred this week
     */
    public List<String> processEvents(List<Stock> stocks) {
        List<String> eventMessages = new ArrayList<>();

        for (Stock stock : stocks) {
            // Roll to see if this stock gets an event this week
            if (random.nextDouble() < EVENT_CHANCE) {
                String message = applyEvent(stock);
                eventMessages.add(message);
            }
        }
        return eventMessages;
    }

    /**
     * Applies a random event to a single stock.
     * Positive events cause a 10-25% price increase.
     * Negative events cause a 10-25% price decrease.
     *
     * @param stock the stock to apply the event to
     * @return a description of what happened
     */
    private String applyEvent(Stock stock) {
        // Randomly decide if this is a positive or negative event
        boolean positive = random.nextBoolean();

        // Random impact between 10% and 25%
        double impact = 0.10 + random.nextDouble() * 0.15;

        BigDecimal multiplier;
        String eventDescription;

        if (positive) {
            // Price goes up
            multiplier = BigDecimal.ONE.add(
                    new BigDecimal(impact).setScale(4, RoundingMode.HALF_UP));
            eventDescription = POSITIVE_EVENTS[random.nextInt(POSITIVE_EVENTS.length)];
        } else {
            // Price goes down
            multiplier = BigDecimal.ONE.subtract(
                    new BigDecimal(impact).setScale(4, RoundingMode.HALF_UP));
            eventDescription = NEGATIVE_EVENTS[random.nextInt(NEGATIVE_EVENTS.length)];
        }

        // Apply the new price, ensuring it never drops below 0.01
        BigDecimal newPrice = stock.getSalesPrice()
                .multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP)
                .max(new BigDecimal("0.01"));

        stock.addNewSalesPrice(newPrice);

        return (positive ? "📈 " : "📉 ")
                + stock.getSymbol() + " — " + eventDescription
                + " (" + (positive ? "+" : "-")
                + String.format("%.1f", impact * 100) + "%)";
    }
}
package no.ntnu.idatx2003.millions.observer;

/**
 * Represents an observer that gets notified when the model changes.
 * Any class that wants to react to model changes must implement this interface.
 * In our case, the GUI view will implement this so it can update the display
 * whenever something changes in the game.
 */
public interface Observer {

    /**
     * Called by the model whenever its state changes.
     * The observer should use this to refresh its display.
     */
    void update();
}
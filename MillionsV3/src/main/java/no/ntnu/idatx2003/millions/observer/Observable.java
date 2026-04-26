package no.ntnu.idatx2003.millions.observer;

/**
 * Represents an object that can be observed.
 * Classes that implement this interface can notify registered observers
 * whenever their state changes.
 * In our case, the model classes implement this so the GUI view
 * gets notified automatically when the game state changes.
 */
public interface Observable {

    /**
     * Registers an observer to be notified of state changes.
     *
     * @param observer the observer to add
     */
    void addObserver(Observer observer);

    /**
     * Removes a previously registered observer.
     *
     * @param observer the observer to remove
     */
    void removeObserver(Observer observer);

    /**
     * Notifies all registered observers that the state has changed.
     * Called internally whenever something meaningful changes.
     */
    void notifyObservers();
}
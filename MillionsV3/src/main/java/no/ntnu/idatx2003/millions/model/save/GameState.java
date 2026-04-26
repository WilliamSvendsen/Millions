package no.ntnu.idatx2003.millions.model.save;

import no.ntnu.idatx2003.millions.model.Exchange;
import no.ntnu.idatx2003.millions.model.Player;

/**
 * Holds the restored game state after loading from a save file.
 * Contains the player and exchange with all their data restored.
 */
public class GameState {

    private final Player player;
    private final Exchange exchange;

    /**
     * Creates a new GameState.
     *
     * @param player   the restored player
     * @param exchange the restored exchange
     */
    public GameState(Player player, Exchange exchange) {
        this.player = player;
        this.exchange = exchange;
    }

    /**
     * Returns the restored player.
     *
     * @return player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the restored exchange.
     *
     * @return exchange
     */
    public Exchange getExchange() {
        return exchange;
    }
}
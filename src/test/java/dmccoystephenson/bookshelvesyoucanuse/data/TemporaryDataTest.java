package dmccoystephenson.bookshelvesyoucanuse.data;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Daniel McCoy Stephenson
 * @since August 28th, 2022
 */
class TemporaryDataTest {
    private TemporaryData temporaryData;
    private Player player;

    @BeforeEach
    void setUp() {
        temporaryData = new TemporaryData();
        player = playerWithId(UUID.randomUUID());
    }

    @Test
    void aPlayerIsNotOnCooldownUntilTheyAreAdded() {
        assertFalse(temporaryData.isPlayerOnCooldown(player));
    }

    @Test
    void addingAPlayerPutsThemOnCooldown() {
        temporaryData.addPlayerToPlayersOnInteractCooldown(player);

        assertTrue(temporaryData.isPlayerOnCooldown(player));
    }

    @Test
    void removingAPlayerTakesThemOffCooldown() {
        temporaryData.addPlayerToPlayersOnInteractCooldown(player);

        temporaryData.removePlayerFromPlayersOnInteractCooldown(player);

        assertFalse(temporaryData.isPlayerOnCooldown(player));
    }

    @Test
    void removingAPlayerWhoWasNeverAddedIsHarmless() {
        temporaryData.removePlayerFromPlayersOnInteractCooldown(player);

        assertFalse(temporaryData.isPlayerOnCooldown(player));
    }

    @Test
    void onePlayersCooldownDoesNotApplyToAnother() {
        Player otherPlayer = playerWithId(UUID.randomUUID());

        temporaryData.addPlayerToPlayersOnInteractCooldown(player);

        assertTrue(temporaryData.isPlayerOnCooldown(player));
        assertFalse(temporaryData.isPlayerOnCooldown(otherPlayer));
    }

    /**
     * The cooldown is keyed on the player's UUID rather than the Player object, so the Player handed
     * to the scheduled removal two seconds later still matches even if Bukkit hands out a different
     * instance for the same account in between.
     */
    @Test
    void theCooldownFollowsThePlayersUuidNotThePlayerInstance() {
        UUID id = UUID.randomUUID();
        Player firstInstance = playerWithId(id);
        Player secondInstance = playerWithId(id);

        temporaryData.addPlayerToPlayersOnInteractCooldown(firstInstance);
        assertTrue(temporaryData.isPlayerOnCooldown(secondInstance));

        temporaryData.removePlayerFromPlayersOnInteractCooldown(secondInstance);
        assertFalse(temporaryData.isPlayerOnCooldown(firstInstance));
    }

    /**
     * Characterizes the list-backed store: a player added twice needs to be removed twice. The
     * interact handler never adds a player who is already on cooldown, so this is not reachable
     * through the plugin, but it is the behaviour any second caller would get today.
     */
    @Test
    void aPlayerAddedTwiceStaysOnCooldownUntilRemovedTwice() {
        temporaryData.addPlayerToPlayersOnInteractCooldown(player);
        temporaryData.addPlayerToPlayersOnInteractCooldown(player);

        temporaryData.removePlayerFromPlayersOnInteractCooldown(player);
        assertTrue(temporaryData.isPlayerOnCooldown(player));

        temporaryData.removePlayerFromPlayersOnInteractCooldown(player);
        assertFalse(temporaryData.isPlayerOnCooldown(player));
    }

    private Player playerWithId(UUID id) {
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(id);
        return player;
    }
}

package dmccoystephenson.bookshelvesyoucanuse.data;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Daniel McCoy Stephenson
 * @since August 28th, 2022
 */
public class TemporaryData {
    private List<UUID> playersOnInteractCooldown = new ArrayList<>();

    public boolean isPlayerOnCooldown(Player player) {
        return playersOnInteractCooldown.contains(player.getUniqueId());
    }

    public void addPlayerToPlayersOnInteractCooldown(Player player) {
        playersOnInteractCooldown.add(player.getUniqueId());
    }

    public void removePlayerFromPlayersOnInteractCooldown(Player player) {
        playersOnInteractCooldown.remove(player.getUniqueId());
    }
}

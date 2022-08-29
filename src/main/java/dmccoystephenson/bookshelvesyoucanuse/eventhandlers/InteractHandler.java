package dmccoystephenson.bookshelvesyoucanuse.eventhandlers;

import dmccoystephenson.bookshelvesyoucanuse.BookshelvesYouCanUse;
import dmccoystephenson.bookshelvesyoucanuse.data.TemporaryData;
import dmccoystephenson.bookshelvesyoucanuse.objects.BookshelfInventory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractHandler implements Listener {
    private TemporaryData temporaryData;
    private BookshelvesYouCanUse bookshelvesYouCanUse;

    public InteractHandler(TemporaryData temporaryData, BookshelvesYouCanUse bookshelvesYouCanUse) {
        this.temporaryData = temporaryData;
        this.bookshelvesYouCanUse = bookshelvesYouCanUse;
    }

    @EventHandler
    public void handle(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        if (temporaryData.isPlayerOnCooldown(player)) {
            return;
        }

        if (block.getType() == Material.BOOKSHELF) {
            player.sendMessage("You rummage through the bookshelf.");
            BookshelfInventory inventory = new BookshelfInventory(block.getLocation());
            player.openInventory(inventory.getInventory());
            temporaryData.addPlayerToPlayersOnInteractCooldown(player);
            removePlayerFromCooldownListWithDelay(player);
        }
    }

    private void removePlayerFromCooldownListWithDelay(Player player) {
        int seconds = 2;
        Bukkit.getServer().getScheduler().runTaskLater(bookshelvesYouCanUse, new Runnable() {
            @Override
            public void run() {
                temporaryData.removePlayerFromPlayersOnInteractCooldown(player);
            }
        }, seconds * 20);
    }
}
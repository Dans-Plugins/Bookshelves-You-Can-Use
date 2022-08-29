package dmccoystephenson.bookshelvesyoucanuse.eventhandlers;

import dmccoystephenson.bookshelvesyoucanuse.BookshelvesYouCanUse;
import dmccoystephenson.bookshelvesyoucanuse.data.TemporaryData;
import dmccoystephenson.bookshelvesyoucanuse.exceptions.BookshelfInventoryNotFoundException;
import dmccoystephenson.bookshelvesyoucanuse.objects.BookshelfInventory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * @author Daniel McCoy Stephenson
 * @since August 28th, 2022
 */
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
            BookshelfInventory inventory;
            try {
                // get existing bookshelf
                inventory = bookshelvesYouCanUse.getBookshelfInventory(block.getLocation());
            } catch (BookshelfInventoryNotFoundException e) {
                // create new bookshelf if not found
                inventory = createBookshelfInventory(block.getLocation());
            }
            player.openInventory(inventory.getInventory());
            temporaryData.addPlayerToPlayersOnInteractCooldown(player);
            removePlayerFromCooldownListWithDelay(player);
        }
    }

    private BookshelfInventory createBookshelfInventory(Location location) {
        BookshelfInventory inventory = new BookshelfInventory(location);
        bookshelvesYouCanUse.getBookshelfInventories().add(inventory);
        return inventory;
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
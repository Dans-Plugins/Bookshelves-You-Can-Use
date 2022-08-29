package dmccoystephenson.bookshelvesyoucanuse.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

/**
 * @author Daniel McCoy Stephenson
 * @since August 28th, 2022
 */
public class BookshelfInventory {
   private int x;
   private int y;
   private int z;
   private String worldName;
   private Inventory inventory = Bukkit.createInventory(null, 9, "Bookshelf");

   public BookshelfInventory(Location location) {
      this.x = location.getBlockX();
      this.y = location.getBlockY();
      this.z = location.getBlockZ();
      this.worldName = location.getWorld().getName();
   }

   public int getX() {
      return x;
   }

   public int getY() {
      return y;
   }

   public int getZ() {
      return z;
   }

   public String getWorldName() {
      return worldName;
   }

   public Inventory getInventory() {
      return inventory;
   }
}
package dmccoystephenson.bookshelvesyoucanuse.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * @author Daniel McCoy Stephenson
 * @since August 28th, 2022
 */
class BookshelfInventoryTest {
    private MockedStatic<Bukkit> bukkit;

    @BeforeEach
    void setUp() {
        bukkit = mockStatic(Bukkit.class);
        // a distinct inventory per call, so that one bookshelf's inventory is distinguishable from another's
        bukkit.when(() -> Bukkit.createInventory(any(), anyInt(), anyString()))
                .thenAnswer(invocation -> mock(Inventory.class));
    }

    @AfterEach
    void tearDown() {
        bukkit.close();
    }

    @Test
    void aBookshelfRemembersTheBlockCoordinatesAndWorldOfItsLocation() {
        BookshelfInventory bookshelf = new BookshelfInventory(location(10, 64, -3, "world"));

        assertEquals(10, bookshelf.getX());
        assertEquals(64, bookshelf.getY());
        assertEquals(-3, bookshelf.getZ());
        assertEquals("world", bookshelf.getWorldName());
    }

    /**
     * A real Location carries doubles; the bookshelf keeps the block the location falls in, which is
     * what lets a later lookup by the clicked block's location find it again.
     */
    @Test
    void aBookshelfKeepsTheBlockALocationFallsInNotItsExactCoordinates() {
        World world = mock(World.class);
        when(world.getName()).thenReturn("world_nether");

        BookshelfInventory bookshelf = new BookshelfInventory(new Location(world, 10.9, 64.5, -3.1));

        assertEquals(10, bookshelf.getX());
        assertEquals(64, bookshelf.getY());
        assertEquals(-4, bookshelf.getZ());
        assertEquals("world_nether", bookshelf.getWorldName());
    }

    /**
     * USER_GUIDE.md promises a 9-slot inventory titled "Bookshelf"; this is where that promise is kept.
     */
    @Test
    void aBookshelfIsBackedByANineSlotInventoryTitledBookshelfWithNoHolder() {
        new BookshelfInventory(location(0, 0, 0, "world"));

        bukkit.verify(() -> Bukkit.createInventory(isNull(), eq(9), eq("Bookshelf")));
    }

    @Test
    void aBookshelfHandsOutTheSameInventoryEveryTime() {
        BookshelfInventory bookshelf = new BookshelfInventory(location(0, 0, 0, "world"));

        assertSame(bookshelf.getInventory(), bookshelf.getInventory());
    }

    @Test
    void eachBookshelfHasItsOwnInventory() {
        BookshelfInventory first = new BookshelfInventory(location(0, 0, 0, "world"));
        BookshelfInventory second = new BookshelfInventory(location(0, 0, 0, "world"));

        assertNotSame(first.getInventory(), second.getInventory());
    }

    private Location location(int x, int y, int z, String worldName) {
        World world = mock(World.class);
        when(world.getName()).thenReturn(worldName);
        Location location = mock(Location.class);
        when(location.getWorld()).thenReturn(world);
        when(location.getBlockX()).thenReturn(x);
        when(location.getBlockY()).thenReturn(y);
        when(location.getBlockZ()).thenReturn(z);
        return location;
    }
}

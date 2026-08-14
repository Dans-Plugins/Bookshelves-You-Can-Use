package dmccoystephenson.bookshelvesyoucanuse.eventhandlers;

import dmccoystephenson.bookshelvesyoucanuse.BookshelvesYouCanUse;
import dmccoystephenson.bookshelvesyoucanuse.data.TemporaryData;
import dmccoystephenson.bookshelvesyoucanuse.exceptions.BookshelfInventoryNotFoundException;
import dmccoystephenson.bookshelvesyoucanuse.objects.BookshelfInventory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Daniel McCoy Stephenson
 * @since August 28th, 2022
 */
class InteractHandlerTest {
    private MockedStatic<Bukkit> bukkit;
    private BukkitScheduler scheduler;
    private BookshelvesYouCanUse plugin;
    private TemporaryData temporaryData;
    private InteractHandler interactHandler;
    private ArrayList<BookshelfInventory> bookshelfInventories;
    private Player player;
    private Block bookshelf;

    @BeforeEach
    void setUp() throws BookshelfInventoryNotFoundException {
        scheduler = mock(BukkitScheduler.class);
        Server server = mock(Server.class);
        when(server.getScheduler()).thenReturn(scheduler);

        bukkit = mockStatic(Bukkit.class);
        bukkit.when(Bukkit::getServer).thenReturn(server);
        // a distinct inventory per call, so that reuse of an existing bookshelf is observable
        bukkit.when(() -> Bukkit.createInventory(any(), anyInt(), anyString()))
                .thenAnswer(invocation -> mock(Inventory.class));

        bookshelfInventories = new ArrayList<>();
        plugin = mock(BookshelvesYouCanUse.class);
        when(plugin.getBookshelfInventories()).thenReturn(bookshelfInventories);
        doThrow(new BookshelfInventoryNotFoundException()).when(plugin).getBookshelfInventory(any());

        temporaryData = new TemporaryData();
        interactHandler = new InteractHandler(temporaryData, plugin);

        player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());

        bookshelf = blockOfType(Material.BOOKSHELF);
    }

    @AfterEach
    void tearDown() {
        bukkit.close();
    }

    @Test
    void rightClickingABookshelfOpensItsInventoryAndCancelsTheEvent() {
        PlayerInteractEvent event = interactEvent(Action.RIGHT_CLICK_BLOCK, bookshelf);

        interactHandler.handle(event);

        assertEquals(1, bookshelfInventories.size());
        verify(player).openInventory(bookshelfInventories.get(0).getInventory());
        verify(player).sendMessage("You rummage through the bookshelf.");
        assertTrue(event.isCancelled());
    }

    @Test
    void leftClickingABookshelfDoesNothing() {
        PlayerInteractEvent event = interactEvent(Action.LEFT_CLICK_BLOCK, bookshelf);

        interactHandler.handle(event);

        verify(player, never()).openInventory(any(Inventory.class));
        verify(player, never()).sendMessage(anyString());
        assertFalse(event.isCancelled());
        assertTrue(bookshelfInventories.isEmpty());
    }

    @Test
    void rightClickingANonBookshelfBlockDoesNothing() {
        PlayerInteractEvent event = interactEvent(Action.RIGHT_CLICK_BLOCK, blockOfType(Material.STONE));

        interactHandler.handle(event);

        verify(player, never()).openInventory(any(Inventory.class));
        assertFalse(event.isCancelled());
        assertTrue(bookshelfInventories.isEmpty());
    }

    @Test
    void rightClickingTheAirDoesNothing() {
        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, null, null, BlockFace.SELF);

        interactHandler.handle(event);

        verify(player, never()).openInventory(any(Inventory.class));
        assertTrue(bookshelfInventories.isEmpty());
    }

    @Test
    void aSecondRightClickWhileOnCooldownIsIgnored() {
        interactHandler.handle(interactEvent(Action.RIGHT_CLICK_BLOCK, bookshelf));
        PlayerInteractEvent secondEvent = interactEvent(Action.RIGHT_CLICK_BLOCK, bookshelf);

        interactHandler.handle(secondEvent);

        verify(player).openInventory(any(Inventory.class));
        assertFalse(secondEvent.isCancelled());
        assertEquals(1, bookshelfInventories.size());
    }

    @Test
    void theScheduledTaskTakesThePlayerOffCooldownAfterTwoSeconds() {
        interactHandler.handle(interactEvent(Action.RIGHT_CLICK_BLOCK, bookshelf));
        assertTrue(temporaryData.isPlayerOnCooldown(player));

        ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTaskLater(eq(plugin), taskCaptor.capture(), eq(40L));
        taskCaptor.getValue().run();

        assertFalse(temporaryData.isPlayerOnCooldown(player));
    }

    @Test
    void anExistingBookshelfInventoryIsReusedInsteadOfRecreated() throws BookshelfInventoryNotFoundException {
        Block knownBookshelf = blockOfType(Material.BOOKSHELF);
        Location knownLocation = knownBookshelf.getLocation();
        BookshelfInventory existingInventory = new BookshelfInventory(knownLocation);
        doReturn(existingInventory).when(plugin).getBookshelfInventory(knownLocation);

        interactHandler.handle(interactEvent(Action.RIGHT_CLICK_BLOCK, knownBookshelf));

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(player).openInventory(inventoryCaptor.capture());
        assertSame(existingInventory.getInventory(), inventoryCaptor.getValue());
        assertTrue(bookshelfInventories.isEmpty());
    }

    private PlayerInteractEvent interactEvent(Action action, Block block) {
        return new PlayerInteractEvent(player, action, null, block, BlockFace.NORTH);
    }

    private Block blockOfType(Material material) {
        World world = mock(World.class);
        when(world.getName()).thenReturn("world");
        Location location = mock(Location.class);
        when(location.getWorld()).thenReturn(world);
        Block block = mock(Block.class);
        when(block.getType()).thenReturn(material);
        when(block.getLocation()).thenReturn(location);
        return block;
    }
}

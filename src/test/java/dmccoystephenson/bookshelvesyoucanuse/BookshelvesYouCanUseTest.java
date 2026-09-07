package dmccoystephenson.bookshelvesyoucanuse;

import dmccoystephenson.bookshelvesyoucanuse.exceptions.BookshelfInventoryNotFoundException;
import dmccoystephenson.bookshelvesyoucanuse.objects.BookshelfInventory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * @author Daniel McCoy Stephenson
 * @since August 28th, 2022
 */
class BookshelvesYouCanUseTest {
    private MockedStatic<Bukkit> bukkit;
    private BookshelvesYouCanUse plugin;

    @BeforeEach
    void setUp() throws BookshelfInventoryNotFoundException {
        bukkit = mockStatic(Bukkit.class);
        // BookshelfInventory's constructor creates its inventory through Bukkit, so a distinct
        // inventory per call keeps the bookshelf a lookup returns identifiable.
        bukkit.when(() -> Bukkit.createInventory(any(), anyInt(), anyString()))
                .thenAnswer(invocation -> mock(Inventory.class));

        plugin = mock(BookshelvesYouCanUse.class);
        doCallRealMethod().when(plugin).configFileExists(any());
        doCallRealMethod().when(plugin).getBookshelfInventory(any());
        doCallRealMethod().when(plugin).isVersionMismatched();
    }

    @AfterEach
    void tearDown() {
        bukkit.close();
    }

    @Test
    void configFileExistsIsFalseForADataFolderWithoutAConfigFile(@TempDir Path dataFolder) {
        assertFalse(plugin.configFileExists(dataFolder.toFile()));
    }

    @Test
    void configFileExistsIsTrueOnceTheConfigFileIsPresentInTheDataFolder(@TempDir Path dataFolder) throws IOException {
        assertTrue(new File(dataFolder.toFile(), "config.yml").createNewFile());

        assertTrue(plugin.configFileExists(dataFolder.toFile()));
    }

    /**
     * The config file is looked for inside the data folder Bukkit provides, rather than at a
     * working-directory-relative path, so two different data folders give two different answers.
     */
    @Test
    void configFileExistsIsResolvedRelativeToTheGivenDataFolder(@TempDir Path populatedFolder, @TempDir Path emptyFolder) throws IOException {
        assertTrue(new File(populatedFolder.toFile(), "config.yml").createNewFile());

        assertTrue(plugin.configFileExists(populatedFolder.toFile()));
        assertFalse(plugin.configFileExists(emptyFolder.toFile()));
    }

    @Test
    void getBookshelfInventoryReturnsTheBookshelfTrackedAtThatLocation() throws BookshelfInventoryNotFoundException {
        BookshelfInventory bookshelf = new BookshelfInventory(location(10, 64, -3, "world"));
        trackBookshelves(bookshelf);

        assertSame(bookshelf, plugin.getBookshelfInventory(location(10, 64, -3, "world")));
    }

    @Test
    void getBookshelfInventoryPicksTheMatchingBookshelfOutOfSeveralTrackedOnes() throws BookshelfInventoryNotFoundException {
        BookshelfInventory wanted = new BookshelfInventory(location(10, 64, -3, "world"));
        trackBookshelves(
                new BookshelfInventory(location(0, 0, 0, "world")),
                wanted,
                new BookshelfInventory(location(10, 64, -4, "world"))
        );

        assertSame(wanted, plugin.getBookshelfInventory(location(10, 64, -3, "world")));
    }

    /**
     * The world name is part of the match, which is what keeps two bookshelves standing at the same
     * coordinates in different worlds from being handed each other's contents.
     */
    @Test
    void getBookshelfInventoryDoesNotMatchTheSameCoordinatesInAnotherWorld() {
        trackBookshelves(new BookshelfInventory(location(10, 64, -3, "world")));

        assertThrows(BookshelfInventoryNotFoundException.class,
                () -> plugin.getBookshelfInventory(location(10, 64, -3, "world_nether")));
    }

    @Test
    void getBookshelfInventoryDoesNotMatchWhenOnlySomeCoordinatesAgree() {
        trackBookshelves(new BookshelfInventory(location(10, 64, -3, "world")));

        assertThrows(BookshelfInventoryNotFoundException.class,
                () -> plugin.getBookshelfInventory(location(11, 64, -3, "world")));
        assertThrows(BookshelfInventoryNotFoundException.class,
                () -> plugin.getBookshelfInventory(location(10, 65, -3, "world")));
        assertThrows(BookshelfInventoryNotFoundException.class,
                () -> plugin.getBookshelfInventory(location(10, 64, -2, "world")));
    }

    @Test
    void getBookshelfInventoryThrowsWhenNoBookshelvesAreTracked() {
        trackBookshelves();

        assertThrows(BookshelfInventoryNotFoundException.class,
                () -> plugin.getBookshelfInventory(location(10, 64, -3, "world")));
    }

    @Test
    void isVersionMismatchedIsFalseWhenTheConfigCarriesNoVersion() {
        givenConfigVersion(null);
        when(plugin.getVersion()).thenReturn("v0.1.0");

        assertFalse(plugin.isVersionMismatched());
    }

    @Test
    void isVersionMismatchedIsFalseWhenThePluginHasNoVersion() {
        givenConfigVersion("v0.1.0");
        when(plugin.getVersion()).thenReturn(null);

        assertFalse(plugin.isVersionMismatched());
    }

    @Test
    void isVersionMismatchedIsFalseWhenTheVersionsAgree() {
        givenConfigVersion("v0.1.0");
        when(plugin.getVersion()).thenReturn("v0.1.0");

        assertFalse(plugin.isVersionMismatched());
    }

    @Test
    void isVersionMismatchedIgnoresCaseWhenComparingTheVersions() {
        givenConfigVersion("V0.1.0-SNAPSHOT");
        when(plugin.getVersion()).thenReturn("v0.1.0-snapshot");

        assertFalse(plugin.isVersionMismatched());
    }

    @Test
    void isVersionMismatchedIsTrueWhenTheVersionsDiffer() {
        givenConfigVersion("v0.0.1");
        when(plugin.getVersion()).thenReturn("v0.1.0");

        assertTrue(plugin.isVersionMismatched());
    }

    private void givenConfigVersion(String version) {
        FileConfiguration config = mock(FileConfiguration.class);
        when(config.getString("version")).thenReturn(version);
        when(plugin.getConfig()).thenReturn(config);
    }

    /**
     * getBookshelfInventory reads the bookshelfInventories field directly rather than through its
     * getter, and Mockito builds the plugin mock without running field initializers, so the list has
     * to be planted on the mock for the real lookup to run against it.
     */
    private void trackBookshelves(BookshelfInventory... bookshelves) {
        try {
            Field field = BookshelvesYouCanUse.class.getDeclaredField("bookshelfInventories");
            field.setAccessible(true);
            field.set(plugin, new ArrayList<>(Arrays.asList(bookshelves)));
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new IllegalStateException("Could not plant the tracked bookshelves on the plugin mock.", exception);
        }
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

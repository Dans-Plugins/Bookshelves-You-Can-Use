package dmccoystephenson.bookshelvesyoucanuse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

/**
 * @author Daniel McCoy Stephenson
 * @since August 28th, 2022
 */
class BookshelvesYouCanUseTest {
    private BookshelvesYouCanUse plugin;

    @BeforeEach
    void setUp() {
        plugin = mock(BookshelvesYouCanUse.class);
        doCallRealMethod().when(plugin).configFileExists(any());
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
}

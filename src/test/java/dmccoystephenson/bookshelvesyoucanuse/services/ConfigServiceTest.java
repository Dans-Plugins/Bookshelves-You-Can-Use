package dmccoystephenson.bookshelvesyoucanuse.services;

import dmccoystephenson.bookshelvesyoucanuse.BookshelvesYouCanUse;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Daniel McCoy Stephenson
 * @since August 28th, 2022
 */
class ConfigServiceTest {
    private BookshelvesYouCanUse plugin;
    private YamlConfiguration config;
    private ConfigService configService;

    @BeforeEach
    void setUp() {
        plugin = mock(BookshelvesYouCanUse.class);
        config = new YamlConfiguration();
        when(plugin.getConfig()).thenReturn(config);
        when(plugin.getVersion()).thenReturn("v0.0.1");
        configService = new ConfigService(plugin);
    }

    @Test
    void saveMissingConfigDefaultsIfNotPresentWritesEveryDocumentedKeyForAnEmptyConfig() {
        configService.saveMissingConfigDefaultsIfNotPresent();

        assertEquals("v0.0.1", config.getString("version"));
        assertTrue(config.isSet("debugMode"));
        assertFalse(config.getBoolean("debugMode"));
        verify(plugin).saveConfig();
    }

    @Test
    void saveMissingConfigDefaultsIfNotPresentOverwritesVersionButPreservesDebugMode() {
        config.set("version", "v0.0.0");
        config.set("debugMode", true);

        configService.saveMissingConfigDefaultsIfNotPresent();

        assertEquals("v0.0.1", config.getString("version"));
        assertTrue(config.getBoolean("debugMode"));
    }

    @Test
    void accessorsReadThroughToThePluginConfig() {
        config.set("debugMode", true);
        config.set("someInt", 7);
        config.set("someDouble", 1.5);
        config.set("someString", "hello");

        assertTrue(configService.isSet("debugMode"));
        assertFalse(configService.isSet("absent"));
        assertTrue(configService.getBoolean("debugMode"));
        assertEquals(7, configService.getInt("someInt"));
        assertEquals(1.5, configService.getDouble("someDouble"));
        assertEquals("hello", configService.getString("someString"));
        assertEquals(config, configService.getConfig());
    }

    @Test
    void orDefaultAccessorsFallBackWhenTheOptionIsAbsent() {
        assertEquals(42, configService.getIntOrDefault("absent", 42));
        assertEquals(4.2, configService.getDoubleOrDefault("absent", 4.2));
        assertEquals("fallback", configService.getStringOrDefault("absent", "fallback"));
    }

    @Test
    void orDefaultAccessorsReturnTheConfiguredValueWhenItIsPresent() {
        config.set("someInt", 7);
        config.set("someDouble", 1.5);
        config.set("someString", "hello");

        assertEquals(7, configService.getIntOrDefault("someInt", 42));
        assertEquals(1.5, configService.getDoubleOrDefault("someDouble", 4.2));
        assertEquals("hello", configService.getStringOrDefault("someString", "fallback"));
    }

    /**
     * Characterizes a known quirk of the numeric or-default accessors: absence is detected by
     * comparing against zero, so a legitimately configured zero is indistinguishable from a
     * missing option and yields the default instead.
     */
    @Test
    void numericOrDefaultAccessorsTreatAConfiguredZeroAsAbsent() {
        config.set("someInt", 0);
        config.set("someDouble", 0.0);

        assertEquals(42, configService.getIntOrDefault("someInt", 42));
        assertEquals(4.2, configService.getDoubleOrDefault("someDouble", 4.2));
    }
}

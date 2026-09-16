package dmccoystephenson.bookshelvesyoucanuse.services;

import dmccoystephenson.bookshelvesyoucanuse.BookshelvesYouCanUse;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
        assertSame(config, configService.getConfig());
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

    /**
     * A server upgraded from before usage reporting has no usage-reporting block in its
     * config.yml, and an existing file is never rewritten with one. Bukkit registers the jar's
     * config.yml as the defaults for the file, so the block has to be read through the defaults.
     * A real YamlConfiguration is used here so the fall-through is measured, not assumed.
     */
    @Test
    void usageReportingReadsThroughToTheBundledDefaultsWhenTheFileHasNoBlock() {
        YamlConfiguration bundled = new YamlConfiguration();
        bundled.set("usage-reporting.enabled", true);
        bundled.set("usage-reporting.endpoint", "https://trace.danielstephenson.dev");
        bundled.set("usage-reporting.key", "bundled-key");
        config.setDefaults(bundled);

        assertFalse(config.isSet("usage-reporting.key"), "the on-disk file itself must lack the block for this test to mean anything");
        assertTrue(configService.isUsageReportingEnabled());
        assertEquals("https://trace.danielstephenson.dev", configService.getUsageReportingEndpoint());
        assertEquals("bundled-key", configService.getUsageReportingKey());
    }

    /**
     * Pins the one-argument getters: the two-argument ones return their explicit fallback
     * instead of the bundled default, which for the key would be "" and would turn reporting
     * off on every existing installation.
     */
    @Test
    void usageReportingUsesTheOneArgumentGettersSoBundledDefaultsApply() {
        FileConfiguration mockedConfig = mock(FileConfiguration.class);
        when(plugin.getConfig()).thenReturn(mockedConfig);
        when(mockedConfig.getBoolean("usage-reporting.enabled")).thenReturn(true);
        when(mockedConfig.getString("usage-reporting.endpoint")).thenReturn("https://trace.danielstephenson.dev");
        when(mockedConfig.getString("usage-reporting.key")).thenReturn("bundled-key");

        assertTrue(configService.isUsageReportingEnabled());
        assertEquals("https://trace.danielstephenson.dev", configService.getUsageReportingEndpoint());
        assertEquals("bundled-key", configService.getUsageReportingKey());
        verify(mockedConfig, never()).getString(eq("usage-reporting.key"), anyString());
        verify(mockedConfig, never()).getString(eq("usage-reporting.endpoint"), anyString());
        verify(mockedConfig, never()).getBoolean(eq("usage-reporting.enabled"), anyBoolean());
    }

    @Test
    void copiesTheBundledUsageReportingBlockOntoAFileThatLacksIt() {
        YamlConfiguration bundled = new YamlConfiguration();
        bundled.set("usage-reporting.enabled", true);
        bundled.set("usage-reporting.endpoint", "https://trace.danielstephenson.dev");
        bundled.set("usage-reporting.key", "bundled-key");
        config.setDefaults(bundled);
        config.set("debugMode", true);
        assertFalse(config.isSet("usage-reporting"), "the on-disk file itself must lack the block for this test to mean anything");

        assertTrue(configService.copyBundledUsageReportingBlockIfAbsent());

        // In the file itself now (read without falling through to the defaults),
        // with the jar's values; everything else untouched; and saved.
        assertEquals(Boolean.TRUE, config.get("usage-reporting.enabled", null));
        assertEquals("https://trace.danielstephenson.dev", config.get("usage-reporting.endpoint", null));
        assertEquals("bundled-key", config.get("usage-reporting.key", null));
        assertTrue(config.getBoolean("debugMode"));
        verify(plugin).saveConfig();
        assertFalse(configService.copyBundledUsageReportingBlockIfAbsent(), "a second pass has nothing to do");
    }

    @Test
    void leavesAnOperatorsOwnUsageReportingBlockAlone() {
        YamlConfiguration bundled = new YamlConfiguration();
        bundled.set("usage-reporting.enabled", true);
        bundled.set("usage-reporting.key", "bundled-key");
        config.setDefaults(bundled);
        config.set("usage-reporting.enabled", false);

        assertFalse(configService.copyBundledUsageReportingBlockIfAbsent());

        assertFalse(configService.isUsageReportingEnabled());
        assertFalse(config.isSet("usage-reporting.key"), "an operator's block is not completed from the jar");
        verify(plugin, never()).saveConfig();
    }

    @Test
    void doesNothingWhenNoBundledBlockExistsEither() {
        assertFalse(configService.copyBundledUsageReportingBlockIfAbsent());
        verify(plugin, never()).saveConfig();
    }

    @Test
    void usageReportingIsOffWithNoKeyAnywhere() {
        assertEquals("", configService.getUsageReportingKey(), "no key anywhere must read as off, not as null");
        assertEquals("https://trace.danielstephenson.dev", configService.getUsageReportingEndpoint());
        assertFalse(configService.isUsageReportingEnabled());
    }

    @Test
    void usageReportingReadsTheConfiguredValuesOverTheBundledDefaults() {
        YamlConfiguration bundled = new YamlConfiguration();
        bundled.set("usage-reporting.enabled", true);
        bundled.set("usage-reporting.endpoint", "https://trace.danielstephenson.dev");
        bundled.set("usage-reporting.key", "bundled-key");
        config.setDefaults(bundled);
        config.set("usage-reporting.enabled", false);
        config.set("usage-reporting.endpoint", "http://localhost:8080");
        config.set("usage-reporting.key", "abc");

        assertFalse(configService.isUsageReportingEnabled());
        assertEquals("http://localhost:8080", configService.getUsageReportingEndpoint());
        assertEquals("abc", configService.getUsageReportingKey());
    }
}

package dmccoystephenson.bookshelvesyoucanuse.services;

/*
    To add a new config option, the following method must be altered:
    - saveMissingConfigDefaultsIfNotPresent()
 */

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import dmccoystephenson.bookshelvesyoucanuse.BookshelvesYouCanUse;

import java.util.Arrays;

/**
 * @author Daniel McCoy Stephenson
 * @since August 28th, 2022
 */
public class ConfigService {
    private static final String USAGE_REPORTING_ENABLED_KEY = "usage-reporting.enabled";
    private static final String USAGE_REPORTING_ENDPOINT_KEY = "usage-reporting.endpoint";
    private static final String USAGE_REPORTING_KEY_KEY = "usage-reporting.key";
    private static final String DEFAULT_USAGE_REPORTING_ENDPOINT = "https://trace.danielstephenson.dev";

    private final BookshelvesYouCanUse plugin;

    public ConfigService(BookshelvesYouCanUse plugin) {
        this.plugin = plugin;
    }

    public void saveMissingConfigDefaultsIfNotPresent() {
        // set version
        if (!getConfig().isString("version")) {
            getConfig().addDefault("version", plugin.getVersion());
        } else {
            getConfig().set("version", plugin.getVersion());
        }

        // save config options
        if (!isSet("debugMode")) { getConfig().set("debugMode", false); }

        getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }

    /**
     * Copies the bundled usage-reporting block into config.yml when the file on
     * disk has none: a file from a version before usage reporting existed, on a
     * server where the version-mismatch save has not run. Reporting was already
     * active there through the one-argument getters below; this makes the switch
     * visible in the file so it can be found and turned off. The values are the
     * jar's defaults, not new literals.
     *
     * @return whether anything was copied (and the file saved)
     */
    public boolean copyBundledUsageReportingBlockIfAbsent() {
        if (isSet("usage-reporting")) {
            return false;
        }
        Configuration defaults = getConfig().getDefaults();
        if (defaults == null || !defaults.isSet("usage-reporting")) {
            return false;
        }
        for (String key : Arrays.asList(USAGE_REPORTING_ENABLED_KEY, USAGE_REPORTING_ENDPOINT_KEY, USAGE_REPORTING_KEY_KEY)) {
            getConfig().set(key, defaults.get(key));
        }
        plugin.saveConfig();
        return true;
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public boolean isSet(String option) {
        return getConfig().isSet(option);
    }

    public int getInt(String option) {
        return getConfig().getInt(option);
    }

    public int getIntOrDefault(String option, int defaultValue) {
        int toReturn = getInt(option);
        if (toReturn == 0) {
            return defaultValue;
        }
        return toReturn;
    }

    public boolean getBoolean(String option) {
        return getConfig().getBoolean(option);
    }

    public double getDouble(String option) {
        return getConfig().getDouble(option);
    }

    public double getDoubleOrDefault(String option, double defaultValue) {
        double toReturn = getDouble(option);
        if (toReturn == 0) {
            return defaultValue;
        }
        return toReturn;
    }

    public String getString(String option) {
        return getConfig().getString(option);
    }

    public String getStringOrDefault(String option, String defaultValue) {
        String toReturn = getString(option);
        if (toReturn == null) {
            return defaultValue;
        }
        return toReturn;
    }

    // The one-argument getters, deliberately. The usage-reporting block lives in
    // the jar's config.yml, and a config.yml that already exists on disk is never
    // rewritten with it, so a server upgraded from a version before usage
    // reporting has no usage-reporting block on disk. Bukkit registers the jar's
    // config.yml as the defaults for that file, and the one-argument getters fall
    // through to them -- but the two-argument getters return their explicit
    // fallback instead, which for the key would be "" and would turn reporting
    // off on every existing installation. Verified against YamlConfiguration,
    // not assumed.

    public boolean isUsageReportingEnabled() {
        return getConfig().getBoolean(USAGE_REPORTING_ENABLED_KEY);
    }

    public String getUsageReportingEndpoint() {
        String endpoint = getConfig().getString(USAGE_REPORTING_ENDPOINT_KEY);
        return endpoint != null ? endpoint : DEFAULT_USAGE_REPORTING_ENDPOINT;
    }

    /** Empty when no key is configured or bundled, which the client treats as "off". */
    public String getUsageReportingKey() {
        String key = getConfig().getString(USAGE_REPORTING_KEY_KEY);
        return key != null ? key : "";
    }
}
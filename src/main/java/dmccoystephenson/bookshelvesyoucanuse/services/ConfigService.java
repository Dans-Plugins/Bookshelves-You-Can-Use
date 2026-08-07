package dmccoystephenson.bookshelvesyoucanuse.services;

/*
    To add a new config option, the following method must be altered:
    - saveMissingConfigDefaultsIfNotPresent()
 */

import org.bukkit.configuration.file.FileConfiguration;
import dmccoystephenson.bookshelvesyoucanuse.BookshelvesYouCanUse;

/**
 * @author Daniel McCoy Stephenson
 * @since August 28th, 2022
 */
public class ConfigService {
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
}
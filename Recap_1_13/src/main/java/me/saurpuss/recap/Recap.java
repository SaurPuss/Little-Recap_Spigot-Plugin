package me.saurpuss.recap;

import me.saurpuss.recap.commands.RecapCommand;
import me.saurpuss.recap.events.JoinNotifyListener;
import me.saurpuss.recap.util.UpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * A small plugin that provides the ability to read and write recap notes for server moderators.
 */
public final class Recap extends JavaPlugin {

    private RecapManager recapManager;
    private final int spigotID = -1; // TODO on first upload

    /**
     * Recap startup logic:
     * - Set up default config if it doesn't exist
     * - Register /recap command & tab completion
     * - Register onJoin event
     * - Set up the RecapManager
     * - Check for plugin updates
     */
    @Override
    public void onEnable() {
        // Set up config
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        // Setup recap command
        RecapCommand recapCMD = new RecapCommand(this);
        getCommand("recap").setExecutor(recapCMD);
        getCommand("recap").setTabCompleter(recapCMD);

        // Register events
        registerEvents();

        // Register recap manager
        recapManager = new RecapManager(this);

        // Poll the spigot site for latest plugin version number
        new UpdateChecker(this, spigotID).getVersion(version -> {
            if (!getDescription().getVersion().equalsIgnoreCase(version)) {
                getLogger().log(Level.INFO, "Version " + version  + " is now available!");
            }
        });
    }

    @Override
    public void onDisable() {}

    public void registerEvents() {
        // Register event listener if notify on join is enabled
        if (getConfig().getBoolean("show-on-join"))
            getServer().getPluginManager().registerEvents(new JoinNotifyListener(this), this);
    }

    public RecapManager getRecapManager() {
        return recapManager;
    }

    public void reloadRecapManager() {
        recapManager = new RecapManager(this);
    }
}

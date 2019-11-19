package me.saurpuss.recap;

import me.saurpuss.recap.commands.RecapCommand;
import me.saurpuss.recap.util.RecapManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * A small plugin that provides the ability to read and write recap notes for server moderators.
 */
public final class Recap extends JavaPlugin {

    /**
     * Manage recap utility where the actual magic happens.
     */
    private static RecapManager recapManager;

    /**
     * LittleRecap startup logic:
     * - Set up default config if it doesn't exist
     * - Register /recap command
     * - Set up the RecapManager
     */
    @Override
    public void onEnable() {
        // Set up config
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        // Register recap command
        getCommand("recap").setExecutor(new RecapCommand(this));

        // Register recap manager
        recapManager = new RecapManager(this);

        // Plugin is set up an running!
        getLogger().log(Level.FINE, "Enabled LittleRecap!");
    }

    /**
     * LittleRecap shutdown logic.
     */
    @Override
    public void onDisable() {}

    /**
     * Access the RecapManager.
     *
     * @return current instance of the RecapManager
     */
    public RecapManager getRecapManager() {
        return recapManager;
    }

    /**
     * Reload the recap manager to read a fresh config
     */
    public void reloadRecapManager() {
        recapManager = new RecapManager(this);
    }
}

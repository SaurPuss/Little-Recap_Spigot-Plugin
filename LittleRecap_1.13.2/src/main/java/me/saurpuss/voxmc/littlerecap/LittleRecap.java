package me.saurpuss.voxmc.littlerecap;

import me.saurpuss.voxmc.littlerecap.commands.Recap;
import me.saurpuss.voxmc.littlerecap.events.UpdateNotification;
import me.saurpuss.voxmc.littlerecap.util.RecapManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 */
public final class LittleRecap extends JavaPlugin {

    /**
     *
     */
    private static RecapManager recapManager;

    /**
     *
     */
    private boolean hasUpdate;

    /**
     * LittleRecap startup logic:
     * - Set up default config if it doesn't exist
     * - Check if there are updates available
     *  - Register update notification on join event
     * - Register /recap command
     * - Set up the RecapManager
     */
    @Override
    public void onEnable() {
        // Set up config
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        // Check if the plugin has updates available
        hasUpdate = checkForUpdates(); // TODO implement this
        if (availableUpdate()) {
            getLogger().info(ChatColor.GREEN + "Update available for LittleRecap!");

            // Register update notification event
            getServer().getPluginManager().registerEvents(new UpdateNotification(this), this);
        }

        // Register recap command
        getCommand("recap").setExecutor(new Recap(this));

        // Register recap manager
        recapManager = new RecapManager(this);

        // Plugin is set up an running!
        getLogger().info(ChatColor.GREEN + "Successfully enabled LittleRecap!");
    }

    /**
     * LittleRecap shutdown logic
     */
    @Override
    public void onDisable() {

    }

    /**
     * Access the RecapManager
     *
     * @return current instance of the RecapManager
     */
    public RecapManager getRecapManager() {
        return recapManager;
    }

    /**
     * Check if there are any newer versions of the plugin available
     *
     * @return boolean true if newer version exists
     */
    private boolean checkForUpdates() {
        return false;
    }

    public boolean availableUpdate() {
        return hasUpdate;
    }
}

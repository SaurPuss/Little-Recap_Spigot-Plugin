package me.saurpuss.voxmc.littlerecap.events;

import me.saurpuss.voxmc.littlerecap.LittleRecap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Event listener to allow players with the permnode recap.use to get the last recaps logged
 * displayed in their chat.
 */
public class RecapListener implements Listener {

    /**
     * Runtime instance of the LittleRecap plugin
     */
    private LittleRecap plugin;

    /**
     * Constructor to get the event up and running
     * @param plugin dependency injection of the current runtime
     */
    public RecapListener(LittleRecap plugin) {
        this.plugin = plugin;
    }

    /**
     * On Join event listener
     * @param event on player join
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("recap.use")) {
            plugin.getRecapManager().getRecapLog().forEach(player::sendMessage);
        }
    }

}

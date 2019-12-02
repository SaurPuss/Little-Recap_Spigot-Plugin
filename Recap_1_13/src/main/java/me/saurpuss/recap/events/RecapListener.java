package me.saurpuss.recap.events;

import me.saurpuss.recap.Recap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Event listener to allow players with the permission node recap.notify to get the last recaps
 * logged displayed in their chat.
 */
public class RecapListener implements Listener {

    /**
     * Runtime instance of the Recap plugin
     */
    private Recap recap;

    /**
     * Constructor to get the event up and running
     *
     * @param plugin dependency injection of the current plugin runtime
     */
    public RecapListener(Recap plugin) {
        recap = plugin;
    }

    /**
     * PlayerJoinEvent Listener that displays the last recap log to players with the recap.notify
     * permission.
     *
     * @param event on player join
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (recap.getConfig().getBoolean("show-online")) {
            if (player.hasPermission("recap.notify")) {
                recap.getRecapManager().getRecapLog().forEach(player::sendMessage);
            }
        }
    }

}

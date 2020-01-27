package me.saurpuss.recap.events;

import me.saurpuss.recap.Recap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Event listener to allow players with the permission node recap.notify to get the last recaps
 * logged displayed in their chat. Only registered if 'show-on-join' in config.yml is true.
 */
public class JoinNotifyListener implements Listener {

    /**
     * Runtime instance of the Recap plugin
     */
    private Recap recap;

    /**
     * Constructor to get the event up and running
     *
     * @param plugin dependency injection of the current plugin runtime
     */
    public JoinNotifyListener(Recap plugin) {
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
        if (!event.getPlayer().hasPermission("recap.notify")) return;

        Player player = event.getPlayer();
        recap.getRecapManager().getRecapLog().forEach(player::sendMessage);
    }

}

package me.saurpuss.recap.events;

import me.saurpuss.recap.Recap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinNotifyListener implements Listener {

    private Recap recap;

    public JoinNotifyListener(Recap plugin) {
        recap = plugin;
    }

    /**
     * PlayerJoinEvent Listener that displays the last recap log to players with the recap.notify
     * permission.
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission("recap.notify")) return;

        Player player = event.getPlayer();
        recap.getRecapManager().getRecapLog().forEach(player::sendMessage);
    }
}

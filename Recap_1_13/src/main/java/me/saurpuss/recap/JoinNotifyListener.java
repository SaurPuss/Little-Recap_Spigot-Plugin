package me.saurpuss.recap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinNotifyListener implements Listener {

    private RecapMain recapMain;

    public JoinNotifyListener(RecapMain plugin) {
        recapMain = plugin;
    }

    /**
     * PlayerJoinEvent Listener that displays the last recap log to players with the recap.notify
     * permission.
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission("recap.notify")) return;

        Player player = event.getPlayer();
        recapMain.getRecapManager().getRecapLog().forEach(player::sendMessage);
    }
}

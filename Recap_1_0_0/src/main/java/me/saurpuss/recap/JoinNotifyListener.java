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
     * PlayerJoinEvent Listener that displays the last recap logs to players with the recap.notify
     * permission. Requires "show-on-join" in config.yml to be true.
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission("recap.notify")) return;

        Player player = event.getPlayer();
        recapMain.getRecapManager().getRecent().forEach(player::sendMessage);
    }
}

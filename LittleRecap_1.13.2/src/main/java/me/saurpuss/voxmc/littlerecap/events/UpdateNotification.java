package me.saurpuss.voxmc.littlerecap.events;

import me.saurpuss.voxmc.littlerecap.LittleRecap;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateNotification implements Listener {

    private LittleRecap plugin;

    public UpdateNotification(LittleRecap plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        if (plugin.availableUpdate() && player.hasPermission("recap.update")) {
            player.sendMessage(ChatColor.GREEN + "Update available for LittleRecap!");
        }
    }
}

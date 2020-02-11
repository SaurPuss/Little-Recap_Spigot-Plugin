package me.saurpuss.recap;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class RecapCommand implements CommandExecutor, TabCompleter {

    private RecapMain recapMain;
    private final boolean notify;

    public RecapCommand(RecapMain plugin) {
        recapMain = plugin;
        notify = plugin.getConfig().getBoolean("notify-live");
    }

    /**
     * /recap               - Display the last 10 recaps
     * /recap reload        - If the CommandSender has permission this reloads
     *                        the plugin config.yml
     * /recap [message...]  - Create an addition to the recap log
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Display the last X recaps
        if (args.length == 0) {
            final List<String> list = recapMain.getRecapManager().getRecent();
            list.forEach(sender::sendMessage);
            return true;
        }

        // Admins with the correct perms are allowed to reload
        if (args[0].equalsIgnoreCase("reload")) {
            if (sender instanceof Player && !sender.hasPermission("recap.reload")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to reload!");
                return false;
            }

            // Reload config & manager
            recapMain.reloadConfig();
            recapMain.reloadRecapManager();

            // Reload events if necessary
            HandlerList.unregisterAll(recapMain);
            recapMain.registerEvents();

            // Notify relevant parties
            for (Player player : Bukkit.getOnlinePlayers())
                if (player.hasPermission("recap.reload.notify"))
                    player.sendMessage(ChatColor.GREEN + "Reloaded plugin!");
            recapMain.getLogger().log(Level.INFO, "Finished reloading plugin!");

            return true;
        }

        // Save the arguments as a string to add to the recap log
        String log = recapMain.getRecapManager().getLogString(sender.getName(),
                StringUtils.join(args, ' '));
        recapMain.getRecapManager().writeLog(log, (toFile, toQueue) -> {
            if (toFile && toQueue) {
                // notify all parties
                if (notify) {
                    for (Player player : Bukkit.getOnlinePlayers())
                        if (player.hasPermission("recap.notify")) {
                            player.sendMessage(ChatColor.GREEN + "[RECAP] " + log);
                        }
                } else {
                    // notify only the sender
                    sender.sendMessage(ChatColor.GREEN + "[RECAP] " + log);
                }
                // Always notify the console
                recapMain.getLogger().log(Level.INFO, log);
            } else if (!toFile) {
                // TODO
                sender.sendMessage(ChatColor.RED + "Failed to write log to file!");
                recapMain.getLogger().log(Level.WARNING, "Failed to write log to file!");
            } else {
                // TODO
                sender.sendMessage(ChatColor.RED + "Failed to write log to memory!");
                recapMain.getLogger().log(Level.WARNING, "Failed to write log to runtime memory!");
            }
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("recap.reload")) return null; // no permission, exit fast

        // In case of additional commands use: Arrays.asList("reload", "other");
        final List<String> COMMANDS = Collections.singletonList("reload");

        return (args.length == 1) ? StringUtil.copyPartialMatches(args[0], COMMANDS,
                new ArrayList<>()) : null;
    }
}

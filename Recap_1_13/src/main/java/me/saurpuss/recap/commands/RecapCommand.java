package me.saurpuss.recap.commands;

import me.saurpuss.recap.Recap;
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

    private Recap recap;

    public RecapCommand(Recap plugin) {
        recap = plugin;
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
            recap.getRecapManager().getRecapLog().forEach(sender::sendMessage);
            return true;
        }

        // Admins with the correct perms are allowed to reload
        if (args[0].equalsIgnoreCase("reload")) {
            if (sender instanceof Player && !sender.hasPermission("recap.reload")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission!");
                return false;
            }

            // Reload config & manager
            recap.reloadConfig();
            recap.reloadRecapManager();

            // Reload events if necessary
            HandlerList.unregisterAll(recap);
            recap.registerEvents();

            // Notify relevant parties
            for (Player player : Bukkit.getOnlinePlayers())
                if (player.hasPermission("recap.reload.notify"))
                    player.sendMessage(ChatColor.GREEN + "Reloaded plugin!");
            recap.getLogger().log(Level.INFO, "Finished reloading plugin!");

            return true;
        }

        // Save the arguments as a string to add to the recap log
        recap.getRecapManager().addRecap(sender, StringUtils.join(args, ' '));
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

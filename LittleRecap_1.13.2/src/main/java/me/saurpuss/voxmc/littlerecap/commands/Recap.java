package me.saurpuss.voxmc.littlerecap.commands;

import me.saurpuss.voxmc.littlerecap.LittleRecap;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 */
public class Recap implements CommandExecutor {

    private LittleRecap plugin;

    public Recap(LittleRecap plugin) {
        this.plugin = plugin;
    }

    /**
     * Implementation of the /recap command
     *   /recap           - Display the last 10 recaps
     *   /recap [reload]  - If the CommandSender has permission this reloads
     *                     the plugin config.yml
     *   /recap [message] - Create an addition to the recap log
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Display the last 10 recaps
        if (args.length == 0) {
            plugin.getRecapManager().getRecapLog().forEach(sender::sendMessage);
            return true;
        }

        // Admins with the correct perms are allowed to reload
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("recap.admin.reload")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission!");
            } else {
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "Reloaded LittleRecap!");
            }
            return true;
        }

        // Save the arguments as a string to add to the recap log
        String message = StringUtils.join(args, ' ');
        plugin.getRecapManager().addRecap(sender, message);

        return true;
    }
}

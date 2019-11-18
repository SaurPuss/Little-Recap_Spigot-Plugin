package me.saurpuss.voxmc.littlerecap.commands;

import me.saurpuss.voxmc.littlerecap.LittleRecap;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * Command implementation for server side recap access.
 */
public class Recap implements CommandExecutor {

    /**
     * Current plugin runtime.
     */
    private LittleRecap plugin;

    /**
     * Command implementation constructor registered in LittleRecap#onEnable()
     * @param plugin dependency injection of the current plugin runtime
     */
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
        if (!sender.hasPermission("recap.use")) return true;

        // Display the last X recaps
        if (args.length == 0) {
            plugin.getRecapManager().getRecapLog().forEach(sender::sendMessage);
            return true;
        }

        // Admins with the correct perms are allowed to reload
        if (args[0].equalsIgnoreCase("reload")) {
            if (sender instanceof Player && !sender.hasPermission("recap.reload")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission!");
            } else {
                plugin.reloadConfig();
                plugin.reloadRecapManager();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("recap.reload")) {
                        player.sendMessage(ChatColor.GREEN + "Reloaded LittleRecap!");
                    }
                }
                plugin.getLogger().log(Level.INFO, "Finished reloading plugin!");
            }
            return true;
        }

        // Save the arguments as a string to add to the recap log
        String message = StringUtils.join(args, ' ');
        plugin.getRecapManager().addRecap(sender, message);

        return true;
    }
}

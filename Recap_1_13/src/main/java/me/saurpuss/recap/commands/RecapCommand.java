package me.saurpuss.recap.commands;

import me.saurpuss.recap.Recap;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * Command implementation for server side recap access.
 */
public class RecapCommand implements CommandExecutor {

    /**
     * Current plugin runtime.
     */
    private Recap recap;

    /**
     * Command implementation constructor registered in Recap#onEnable()
     *
     * @param plugin dependency injection of the current plugin runtime
     */
    public RecapCommand(Recap plugin) {
        recap = plugin;
    }

    /**
     * Implementation of the /recap command:
     * /recap           - Display the last 10 recaps
     * /recap reload    - If the CommandSender has permission this reloads
     *                    the plugin config.yml
     * /recap [message] - Create an addition to the recap log
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
            recap.reloadConfig();
            recap.reloadRecapManager();

            // TODO reload event registration

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("recap.notify")) {
                    // TODO reload notify permission
                    player.sendMessage(ChatColor.GREEN + "Reloaded Recap!");
                }
            }

            recap.getLogger().log(Level.INFO, "Finished reloading plugin!");
            return true;
        }

        // Save the arguments as a string to add to the recap log
        recap.getRecapManager().addRecap(sender, StringUtils.join(args, ' '));
        return true;
    }
}

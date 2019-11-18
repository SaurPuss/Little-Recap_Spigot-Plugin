package me.saurpuss.voxmc.littlerecap.util;

import me.saurpuss.voxmc.littlerecap.LittleRecap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;

/**
 * Recap Manager Utility class, implements command functionality and file access.
 */
public class RecapManager {

    /**
     * Current plugin runtime.
     */
    private LittleRecap plugin;

    /**
     * Preferred date formatting for recap logs. Defaults to "MMM dd" if no valid format is found.
     * Uses java DateTimeFormatter class.
     */
    private final DateTimeFormatter formatter;

    /**
     * Maximum size of the runtime recap log, defined in config.yml.
     */
    private final int maxSize;

    /**
     * Add the command executor to the recap log, defined in config.yml.
     */
    private final boolean logAuthor;

    /**
     * Display the recap log to the online players with the recap.use perm node when a new log is
     * made.
     */
    private final boolean showOnline;

    /**
     * Allow the recap message to have color codes, defined in config.yml.
     */
    private final boolean allowColors;

    /**
     * Add each recap log to the recap.txt file, or overwrite the recap.txt file with the entire
     * runtime recap log.
     */
    private final boolean appendLog;

    /**
     * Runtime definition of recap.txt.
     */
    private File recapFile;

    /**
     * Runtime recap log accessed by the recap command.
     */
    private static Deque<String> recapLog;

    /**
     * Recap Manager constructor. Used to set up the file and variables and control access to the
     * recap functionality for the recap command.
     * @param littleRecap dependency injection of the current plugin runtime
     */
    public RecapManager(LittleRecap littleRecap) {
        plugin = littleRecap;
        FileConfiguration config = plugin.getConfig();

        // Get preferences
        formatter = DateTimeFormatter.ofPattern(Objects.requireNonNull(config.getString("date-format")));
        maxSize = Math.abs(config.getInt("max-size"));
        logAuthor = config.getBoolean("log-author");
        showOnline = config.getBoolean("show-online");
        allowColors = config.getBoolean("allow-colors");
        appendLog = config.getBoolean("append-log");

        // Set up recap.txt (if necessary)
        recapFile = new File(this.plugin.getDataFolder(), "recap.txt");
        if (!recapFile.exists() || recapFile.length() == 0)
            makeRecapLog();

        // Try to retrieve existing recap logs
        recapLog = populateRecap();
    }

    /**
     * Get the current copy of the runtime recap log.
     * @return Linked List with the latest recaps
     */
    public Deque<String> getRecapLog() {
        return recapLog;
    }

    /**
     * Functional implementation of the recap command, adding a log line to the file and the
     * runtime recap log.
     * @param sender Command Executor from the recap command
     * @param message Message to be logged
     */
    public void addRecap(CommandSender sender, String message) {
        // Set up the log based on preferences in the config
        String log = ChatColor.RED + LocalDate.now().format(formatter) + " §6-§c " +
                (logAuthor ? sender.getName() + "§6: §r" : "§r") + (allowColors ?
                ChatColor.translateAlternateColorCodes('&', message) : message);

        // Add log to the linked list
        recapLog.addFirst(log);
        if (recapLog.size() > maxSize)
            recapLog.removeLast();

        // Save to file
        boolean success = appendLog ? appendRecap(log) : overrideRecap();

        // Notify command executor
        sender.sendMessage(success ? ChatColor.GREEN + "Successfully added recap!" :
                ChatColor.RED + "Failed to add recap!");
        if (showOnline) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("recap.use"))
                    player.sendMessage(ChatColor.GREEN + "[RECAP] " + recapLog.getFirst());
            }
        }
        Bukkit.getConsoleSender().sendMessage("[RECAP] " + recapLog.getFirst());
    }

    /**
     * Add a line to recap.txt with the latest log.
     * @param log Formatted message to be logged in the recap.txt file
     * @return success
     */
    private boolean appendRecap(String log) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(recapFile, true));
            writer.println(log);
            writer.close();
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to log recap to recap.txt!" + e);
            return false;
        }
    }

    /**
     * Override the current recap.txt with the updated version of the runtime recap log.
     * @return success
     */
    private boolean overrideRecap() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(recapFile, false));
            recapLog.forEach(writer::println);
            writer.close();
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to log recap to recap.txt!" + e);
            return false;
        }
    }

    /**
     * Populate the runtime recap log with information from the recap.txt file, sorted from
     * newest to oldest.
     * @return Linked List filled with the recap logs
     */
    private Deque<String> populateRecap() {
        ArrayList<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(recapFile), Charset.defaultCharset()))) {
            String line;
            while ((line = reader.readLine()) != null)
                list.add(line);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error when trying to retrieve the logs " +
                    "from recap.txt! Printing StackTrace and Disabling LittleRecap!" + e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

        Deque<String> log = new LinkedList<>();
        // If append is true, make sure to get the last X entries
        if (appendLog) Collections.reverse(list);
        for (int i = 0; i < maxSize && i < list.size(); i++)
            log.add(list.get(i));

        return log;
    }

    /**
     * Create the recap.txt file in the config folder and attempt to write a line to the file. If
     * either of these fails the plugin will be disabled and a stacktrace will be printed to the
     * console.
     */
    private void makeRecapLog() {
        plugin.getLogger().log(Level.INFO, "Could not find a valid recap.txt file in " +
                "the config folder, attempting to create a new file!");

        try {
            // Create file and directory if necessary
            boolean fil = recapFile.createNewFile();
            if (fil)
                plugin.getLogger().log(Level.INFO, "Created recap.txt!");
            else
                plugin.getLogger().log(Level.WARNING, "recap.txt already exists!");
        } catch (IOException e) {
            // Can't continue without a working log, disable the plugin
            plugin.getLogger().log(Level.SEVERE,
                    "Can't create recap.txt! Disabling LittleRecap! " + e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

        try {
            // Try to write to the fresh file
            plugin.getLogger().log(Level.INFO, "Attempting to write to recap.txt!");

            PrintWriter writer = new PrintWriter(new FileWriter(recapFile, true), true);
            writer.println("§c" + LocalDateTime.now().format(formatter) + "§6 - §rUse §d/recap " +
                    "[message here...] §rto add a recap!");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE,
                    "Can't write to recap.txt! Disabling LittleRecap! " + e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

        plugin.getLogger().log(Level.INFO, "Successfully set up recap.txt! Happy logging!");
    }
}

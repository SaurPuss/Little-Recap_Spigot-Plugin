package me.saurpuss.voxmc.littlerecap.util;

import me.saurpuss.voxmc.littlerecap.LittleRecap;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;

public class RecapManager {

    // Dependency Injection
    private LittleRecap plugin;

    // plugin settings
    private String dateFormat;
    private int maxSize;
    private boolean logAuthor;
    private boolean allowColors;
    private boolean appendLog;

    // recap log
    private File recapFile;
    private static Deque<String> recapLog;

    public RecapManager(LittleRecap plugin) {
        this.plugin = plugin;

        // Set up recap.txt (if necessary)
        recapFile = new File(this.plugin.getDataFolder(), "recap.txt");
        if (recapFile.length() == 0)
            makeRecapLog();

        // Set up choices from the config
        recapPreferences();

        // Try to retrieve existing recap logs
        recapLog = populateRecap();
    }

    public Deque<String> getRecapLog() {
        return recapLog;
    }



    public void addRecap(CommandSender sender, String message) {
        if (dateFormat == null) dateFormat = "MMM dd"; // default formatter

        // Format the log based on preferences in the config
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        LocalDate date = LocalDate.now();

        String log = ChatColor.RED + date.format(formatter) + "§6 - §c" +
                (logAuthor ? sender.getName() + "§6: §r" : "§r") +
                (allowColors ? ChatColor.translateAlternateColorCodes(
                        '&', message) : message);

        // Add log to the linked list
        recapLog.addFirst(log);

        if (recapLog.size() > maxSize)
            recapLog.removeLast();

        // Save to file
        boolean success = appendLog ? appendRecap(log) : overrideRecap();

        // Notify player
        sender.sendMessage(success ?
                ChatColor.RED + "Failed to add recap!" :
                ChatColor.GREEN + "Successfully added recap!");
        plugin.getLogger().info("[RECAP] " + log);
    }


    private boolean appendRecap(String log) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(recapFile, true), true)) {
            writer.println(log);
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to log recap to recap.txt!");
            e.printStackTrace();
            return false;
        }
    }

    private boolean overrideRecap() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(recapFile, false), true)) {
            recapLog.forEach(writer::println);
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to log recap to recap.txt!");
            e.printStackTrace();
            return false;
        }
    }


    private Deque<String> populateRecap() {
        if (!recapFile.exists()) makeRecapLog(); // safety net

        ArrayList<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(recapFile),
                        Charset.defaultCharset()))) {
            String line;
            while ((line = reader.readLine()) != null)
                list.add(line);

        } catch (IOException e) {
            plugin.getLogger().warning("Fatal error when trying to" +
                    " retrieve the logs from recap.txt! Disabling " +
                    " LittleRecap and printing StackTrace!");
            e.printStackTrace();
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

        // If append is true, make sure to get the last 10 entries
        if (appendLog)
            list.sort(Collections.reverseOrder());

        Deque<String> log = new LinkedList<>();
        for (int i = 0; i < 10 && i < list.size() - 1; i++)
            log.add(list.get(i));

        return log;
    }

    /**
     * Create the recap.txt file in the config folder and attempt to write
     * a line to the file. If either of these fails the plugin will be
     * disabled and a stacktrace will be printed to the console.
     */
    private void makeRecapLog() {
        plugin.getLogger().info(ChatColor.DARK_GREEN +
                "Could not find recap.txt in the config folder," +
                " attempting to create a new file!");

        try {
            // Create file and directory if necessary
            recapFile.getParentFile().mkdirs();
            recapFile.createNewFile();
        } catch (IOException e) {
            // Can't continue without a working log, disable the plugin
            plugin.getLogger().warning(ChatColor.RED + "Can't create recap.txt!" +
                    " Disabling LittleRecap and printing StackTrace!");
            e.printStackTrace();
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

        try {
            // Try to write to the fresh file
            plugin.getLogger().info(ChatColor.DARK_GREEN +
                    "Attempting to write to recap.txt!");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
            LocalDate date = LocalDate.now();

            PrintWriter writer = new PrintWriter(new FileWriter(recapFile, true), true);
            writer.println("§c" + date.format(formatter) + ": §6Use §d/recap [message here...]" +
                    " §6to add a recap!");
        } catch (IOException e) {
            plugin.getLogger().warning(ChatColor.RED + "Can't write to recap.txt!" +
                    " Disabling LittleRecap and printing StackTrace!");
            e.printStackTrace();
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

        plugin.getLogger().info(ChatColor.GREEN + "Successfully set up recap.txt!" +
                " Happy logging!");
    }

    /**
     * Retrieve logging preferences from config.yml and set them to
     * their String and boolean variables to reference in the runtime.
     */
    private void recapPreferences() {
        dateFormat = plugin.getConfig().getString("date-format");
        maxSize = plugin.getConfig().getInt("max-size");
        logAuthor = plugin.getConfig().getBoolean("log-author");
        allowColors = plugin.getConfig().getBoolean("allow-colors");
        appendLog = plugin.getConfig().getBoolean("append-log");
    }
}

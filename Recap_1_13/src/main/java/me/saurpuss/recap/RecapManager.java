package me.saurpuss.recap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.charset.Charset;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.logging.Level;

/**
 * Recap Manager Utility class, implements command functionality and file access.
 */
public class RecapManager {

    private RecapMain plugin;
    private final DateTimeFormatter formatter;

    // Config preferences
    private final int maxSize;
    private final boolean logAuthor;
    private final boolean showLive;
    private final boolean allowColors;
    private final boolean appendLog;
    private final boolean fifo;

    // Logging things
    private final File recapFile;
    private final ReadWriteLock fileLock;
    private volatile Deque<String> recapLog; // TODO

    public RecapManager(RecapMain recapMain) {
        plugin = recapMain;
        fileLock = new ReentrantReadWriteLock();

        // Get preferences
        final FileConfiguration config = plugin.getConfig();
        final String format = config.getString("date-format");
        formatter = DateTimeFormatter.ofPattern(format != null ? format : "MMM dd");
        maxSize = Math.abs(config.getInt("max-size"));
        logAuthor = config.getBoolean("log-author");
        showLive = config.getBoolean("notify-live");
        allowColors = config.getBoolean("allow-colors");
        appendLog = config.getBoolean("append-log"); // TODO
        fifo = config.getBoolean("fifo-sorting"); // TODO

        // Set up recap.txt (if necessary)
        recapFile = new File(plugin.getDataFolder(), "recap.txt");
        if (!recapFile.exists()) {
            try {
                recapFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Can't load file writer/reader, " +
                        "disabling plugin!", e);
                Bukkit.getPluginManager().disablePlugin(plugin);
                return;
            }
        }

        // Try to retrieve existing recap logs
        if (recapFile.length() == 0)
            makeRecapLog();
        recapLog = populateRecap();
    }

    public Deque<String> getRecapLog() {
        return recapLog;
    }

    /**
     * Functional implementation of the recap command, adding a log line to the file and the
     * runtime recap log. Example recap:
     * §cAUG 02 §6-§c Zombiemold§6:§r Recap message here.
     *
     * @param sender  Command Executor from the recap command
     * @param message Message to be logged
     */
    public void addRecap(CommandSender sender, String message) {
        // Set up the log based on preferences in the config
        String log = ChatColor.RED + LocalDate.now().format(formatter) + " §6-§c " +
                (logAuthor ? sender.getName() + "§6: §r" : "§r") + (allowColors ?
                ChatColor.translateAlternateColorCodes('&', message) : message);

        // Add log to the linked list
        recapLog.addFirst(log); // TODO fifo
        if (recapLog.size() > maxSize)
            recapLog.removeLast();

        // Save to file
        boolean success = appendLog ? appendRecap(log) : overrideRecap();

        // Notify command executor
        sender.sendMessage(success ? ChatColor.GREEN + "Successfully added recap!" :
                ChatColor.RED + "Failed to add recap!");
        if (showLive) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("recap.notify"))
                    player.sendMessage(ChatColor.GREEN + "[RECAP] " + recapLog.getFirst());
            }
        }

        plugin.getLogger().log(Level.INFO, recapLog.getFirst());
    }

    /**
     * Add a line to recap.txt with the latest log.
     *
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
     *
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
     *
     * @return Linked List filled with the recap logs
     */
    private Deque<String> populateRecap() {
        List<String> list = read();
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
        plugin.getLogger().log(Level.INFO, "Attempting to write to recap.txt!");

        final String log = "§c" + LocalDateTime.now().format(formatter) + "§6 - §rUse " +
                "§d/recap [message here...] §rto add a recap!";

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = write(log);

            if(!success) {
                plugin.getLogger().log(Level.SEVERE, "Can't write to recap.txt! Disabling plugin!");
                Bukkit.getScheduler().runTask(plugin, () -> // TODO do I need to sync this?
                        plugin.getServer().getPluginManager().disablePlugin(plugin));
            } else {
                plugin.getLogger().log(Level.INFO, "Successfully created recap.txt!");
                recapLog.add(log);
            }
        });
    }

    private boolean write(String log) {
        Lock writeLock = fileLock.writeLock();
        writeLock.lock();

        try (PrintWriter writer = new PrintWriter(new FileWriter(recapFile, true), true)) {
            // Try to write to the fresh file
            writer.println(log);




            return true;
        } catch (IOException e) {
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    private List<String> read() {
        Lock readLock = fileLock.readLock();
        readLock.lock(); // TODO

        List<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(recapFile), Charset.defaultCharset()))) {
            String line;
            while ((line = reader.readLine()) != null)
                list.add(line);

            return list;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error when trying to retrieve the logs " +
                    "from recap.txt! Printing StackTrace and Disabling LittleRecap!" + e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        } finally {
            readLock.unlock();
        }

        return null;
    }
}

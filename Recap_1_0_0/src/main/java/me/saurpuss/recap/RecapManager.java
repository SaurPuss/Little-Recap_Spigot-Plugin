package me.saurpuss.recap;

import me.saurpuss.recap.util.FileWriteCallback;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
    private final boolean allowColors;

    // Recap file things
    private final File recapFile;
    private final ReadWriteLock fileLock = new ReentrantReadWriteLock();
    private final Lock fileWriteLock = fileLock.writeLock();
    private final Lock fileReadLock = fileLock.readLock();

    // Runtime recap
    private final ReadWriteLock queueLock = new ReentrantReadWriteLock();
    private final Lock queueWriteLock = queueLock.writeLock();
    private Deque<String> recent;

    public RecapManager(RecapMain recap) {
        plugin = recap;

        // Get preferences
        final FileConfiguration config = plugin.getConfig();
        final String format = config.getString("date-format");
        formatter = DateTimeFormatter.ofPattern(format != null ? format : "MMM dd");
        maxSize = Math.abs(config.getInt("max-size"));
        logAuthor = config.getBoolean("log-author");
        allowColors = config.getBoolean("allow-colors");

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
        if (recapFile.length() == 0) setupRecapFile();
        recent = setupRecapQueue();
    }

    public List<String> getRecent() {
        final ArrayList<String> result;
        result = new ArrayList<>(recent);
        return result;
    }

    public String getLogString(final String sender, final String message) {
        return ChatColor.RED + LocalDate.now().format(formatter) + " §6-§c " +
                (logAuthor ? sender + "§6: §r" : "§r") + (allowColors ?
                ChatColor.translateAlternateColorCodes('&', message) : message);
    }

    public void writeLog(String log, FileWriteCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            final boolean toFile = writeToFile(log);
            final boolean toQueue = writeToQueue(log);

            Bukkit.getScheduler().runTask(plugin, () -> callback.fileWriteCallback(toFile, toQueue));
        });
    }

    private boolean writeToQueue(String log) {
        queueWriteLock.lock();
        try {
            recent.addLast(log);
            if (recent.size() > maxSize) recent.removeFirst();
            return true;
        } finally {
            queueWriteLock.unlock();
        }
    }

    /**
     * Add a line to the recap.txt file
     * @param log date, author (optional), and message to be saved into the file
     * @return true if successful
     */
    private boolean writeToFile(String log) {
        fileWriteLock.lock();
        try (PrintWriter writer = new PrintWriter(new FileWriter(recapFile, true), true)) {
            writer.println(log);
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to write to recap log!", e);
        } finally {
            fileWriteLock.unlock();
        }
        return false;
    }

    /**
     * Populate the runtime recap log with information from the recap.txt file, sorted from
     * newest to oldest.
     *
     * @return Linked List filled with the recap logs
     */
    private Deque<String> setupRecapQueue() {
        List<String> list = new ArrayList<>();
        Deque<String> log = new LinkedList<>();

        fileReadLock.lock();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(recapFile), Charset.defaultCharset()))) {
            String line;
            while ((line = reader.readLine()) != null)
                list.add(line);

        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error when trying to retrieve the logs " +
                    "from recap.txt! Printing StackTrace and disabling plugin!" + e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return log;
        } finally {
            fileReadLock.unlock();
        }

        Collections.reverse(list);
        for (int i = 0; i < maxSize && i < list.size(); i++)
            log.addFirst(list.get(i));

        return log;
    }

    /**
     * Create the recap.txt file in the config folder if necessary and attempt to write a line to
     * the file. If either of these fails the plugin will be disabled.
     */
    private void setupRecapFile() {
        plugin.getLogger().log(Level.INFO, "Attempting to write to recap.txt!");
        final String log = "§c" + LocalDateTime.now().format(formatter) + "§6 - §rUse " +
                "§d/recap [message here...] §rto add a recap!";

        writeToFile(log);
        plugin.getLogger().log(Level.INFO, "Finished creating recap.txt!");
    }
}

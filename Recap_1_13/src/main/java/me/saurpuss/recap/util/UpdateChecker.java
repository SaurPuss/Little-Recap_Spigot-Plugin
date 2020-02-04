package me.saurpuss.recap.util;

import me.saurpuss.recap.Recap;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.logging.Level;

public class UpdateChecker {

    private final Recap plugin;
    private final int id;

    public UpdateChecker(Recap recap, int id) {
        plugin = recap;
        this.id = id;
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (InputStream inputStream = new URL(
                    "https://api.spigotmc.org/legacy/update.php?resource=" + id).openStream();
                 Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException exception) {
                plugin.getLogger().log(Level.WARNING, "Error while checking for plugin updates!",
                        exception.getMessage());
            }
        });
    }
}
package com.keurig.combatlogger.utils;

import com.keurig.combatlogger.CombatLoggerPlugin;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class Updater {

    private CombatLoggerPlugin plugin;

    public static String VERSION = null;

    public Updater(CombatLoggerPlugin plugin) {
        this.plugin = plugin;
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + 85755).openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException exception) {
                Bukkit.getLogger().info("Unable to check for updates: " + exception.getMessage());
            }
        });
    }
}

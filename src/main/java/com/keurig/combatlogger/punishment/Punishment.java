package com.keurig.combatlogger.punishment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public abstract class Punishment implements Listener {

    /**
     * Get the player who logged out during combat.
     */
    private Player player;

    /**
     * Get the name of the punishment used in config.yml use `UPPER CASE` format.
     */
    private final String name;

    /**
     * Get the amount of args required for the punishment, if none then not needed.
     */
    private final int numberArgs;

    /**
     * Get the punishment config
     */
    private PunishmentConfig punishmentConfig;

    private Map<String, Object> args;

    public Punishment(String name) {
        this(name, 0);
    }

    public Punishment(String name, int numberArgs) {
        this.name = name;
        this.numberArgs = numberArgs;
        this.args = new HashMap<>();
    }

    /**
     * Runs when the player quits the server during combat.
     *
     * @param label the name of the punishment
     */
    public abstract void onQuit(String label);

    @Override
    public String toString() {
        return name;
    }
}

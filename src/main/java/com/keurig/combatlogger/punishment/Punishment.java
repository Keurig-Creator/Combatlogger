package com.keurig.combatlogger.punishment;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class Punishment implements Listener {

    private final PunishmentHandler handler;

    @Getter
    @Setter
    private Player player;

    @Getter
    private final String label;

    public Punishment(PunishmentHandler handler, String label) {
        this.handler = handler;
        this.label = label;
    }

    public abstract void runPunishment();

    public String getString(String key) {
        System.out.println("punishment" + "." + label + "." + key);
        return handler.getConfig().getString(handler.getPunishmentPath() + "." + label + "." + key);
    }

    public int getInt(String key) {
        return handler.getConfig().getInt(handler.getPunishmentPath() + "." + label + "." + key);
    }
}

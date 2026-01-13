package com.keurig.combatlogger.handler;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class TagInfo {

    public enum Cause {PVP, ENVIRONMENT, COMMAND, UNKNOWN}

    private final UUID victim;
    private final UUID tagger; // null if no player
    private final Cause cause;
    private final long taggedAtMillis;

    public TagInfo(UUID victim, UUID tagger, Cause cause, long taggedAtMillis) {
        this.victim = victim;
        this.tagger = tagger;
        this.cause = cause;
        this.taggedAtMillis = taggedAtMillis;
    }

    public UUID getVictim() {
        return victim;
    }

    public UUID getTagger() {
        return tagger;
    }

    public Cause getCause() {
        return cause;
    }

    public long getTaggedAtMillis() {
        return taggedAtMillis;
    }

    public OfflinePlayer getVictimPlayer() {
        return Bukkit.getOfflinePlayer(victim);
    }

    public OfflinePlayer getTaggerPlayer() {
        return tagger == null ? null : Bukkit.getOfflinePlayer(tagger);
    }
}
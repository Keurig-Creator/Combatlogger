package com.keurig.combatlogger.punishment.punishments;

import com.keurig.combatlogger.punishment.Punishment;

public class WhitelistPunishment extends Punishment {

    public WhitelistPunishment() {
        super("WHITELIST");
    }

    @Override
    public void onQuit(String label) {
        if (getPlayer().hasPermission("combatlogger.admin"))
            return;

        getPlayer().setWhitelisted(false);
    }
}

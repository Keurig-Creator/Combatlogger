package com.keurig.combatlogger.punishment.data;

import com.keurig.combatlogger.punishment.Punishment;
import com.keurig.combatlogger.punishment.PunishmentHandler;

public class KillPunishment extends Punishment {

    public KillPunishment(PunishmentHandler handler) {
        super(handler, "kill");
    }

    @Override
    public void runPunishment() {

        System.out.println("SENDING");

    }
}

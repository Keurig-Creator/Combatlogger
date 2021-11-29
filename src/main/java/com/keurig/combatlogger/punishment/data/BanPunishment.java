package com.keurig.combatlogger.punishment.data;

import com.keurig.combatlogger.punishment.Punishment;
import com.keurig.combatlogger.punishment.PunishmentHandler;

public class BanPunishment extends Punishment {

    public BanPunishment(PunishmentHandler handler) {
        super(handler, "ban");
    }

    @Override
    public void runPunishment() {

        System.out.println("SENDING");
        System.out.println(getString("message"));
        System.out.println(getInt("time"));
        getPlayer().sendMessage("Sheesh" + getString("message"));
    }
}

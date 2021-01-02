package com.keurig.combatlogger.utils.factions;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.listeners.CommandFlightListener;
import com.keurig.combatlogger.listeners.FlightListener;
import com.keurig.combatlogger.utils.Chat;
import lombok.Getter;
import org.bukkit.Bukkit;

@Getter
public class FactionsManager {

	private FactionsHook factionsHook;

	private final boolean factionsEnabled;

	public FactionsManager() {
		CombatLogger plugin = CombatLogger.getInstance();

		if (Bukkit.getPluginManager().isPluginEnabled("Factions")) {
			this.factionsHook = new FactionsUUID();
			Bukkit.getPluginManager().registerEvents(new CommandFlightListener(), plugin);

			this.factionsEnabled = true;

			Chat.log("&aEnabled FactionsUUID Support");
		} else if (Bukkit.getPluginManager().isPluginEnabled("FactionsX")) {
			this.factionsHook = new FactionsX();
			Bukkit.getPluginManager().registerEvents(new FlightListener(), plugin);

			this.factionsEnabled = true;

			Chat.log("&aEnabled FactionsX Support");
		} else {
			this.factionsEnabled = false;

			Chat.log("&cFactions not found! Disabling faction support");
		}

	}
}

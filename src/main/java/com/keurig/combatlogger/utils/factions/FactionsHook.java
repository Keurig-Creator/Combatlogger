package com.keurig.combatlogger.utils.factions;

import org.bukkit.entity.Player;

import java.util.List;

public interface FactionsHook {

	void disableFlight(Player player);

	List<String> aliases();

	List<String> subAliases();

}

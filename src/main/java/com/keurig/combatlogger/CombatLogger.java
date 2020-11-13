package com.keurig.combatlogger;

import com.keurig.combatlogger.handler.CombatPlayer;
import com.keurig.combatlogger.listeners.AttackListener;
import com.keurig.combatlogger.listeners.JoinListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class CombatLogger extends JavaPlugin {

	private CombatPlayer combatPlayer;

	@Override
	public void onEnable() {

		combatPlayer = new CombatPlayer(this);

		getConfig().options().copyDefaults();
		saveDefaultConfig();

		Bukkit.getPluginManager().registerEvents(new AttackListener(this), this);
		Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);
	}

	public CombatPlayer getCombatPlayer() {
		return combatPlayer;
	}
}

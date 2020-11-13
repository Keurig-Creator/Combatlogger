package com.keurig.combatlogger;

import com.keurig.combatlogger.listeners.AttackListener;
import com.keurig.combatlogger.listeners.QuitListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class CombatLogger extends JavaPlugin {

	private Map<Player, Long> combatLogged;

	@Override
	public void onEnable() {

		getConfig().options().copyDefaults();
		saveDefaultConfig();

		Bukkit.getPluginManager().registerEvents(new AttackListener(this), this);
		Bukkit.getPluginManager().registerEvents(new QuitListener(this), this);
		combatLogged = new HashMap<Player, Long>();
	}

	@Override
	public void onDisable() {
		combatLogged.clear();
	}

	public Map<Player, Long> getCombatLogged() {
		return combatLogged;
	}
}

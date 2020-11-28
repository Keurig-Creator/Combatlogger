package com.keurig.combatlogger;

import com.keurig.combatlogger.handler.CombatPlayer;
import com.keurig.combatlogger.listeners.AttackListener;
import com.keurig.combatlogger.listeners.DeathListener;
import com.keurig.combatlogger.listeners.JoinListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class CombatLogger extends JavaPlugin {

	@Getter
	private static CombatLogger instance;

	private CombatPlayer combatPlayer;

	@Override
	public void onEnable() {

		this.combatPlayer = new CombatPlayer(this);

		getConfig().options().copyDefaults();
		saveDefaultConfig();

		Bukkit.getPluginManager().registerEvents(new AttackListener(this), this);
		Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);
		Bukkit.getPluginManager().registerEvents(new DeathListener(this), this);
	}

	public boolean isTagged(Player player) {
		return this.combatPlayer.isTagged(player);
	}
}

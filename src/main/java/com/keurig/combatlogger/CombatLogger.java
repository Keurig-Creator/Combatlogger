package com.keurig.combatlogger;

import com.keurig.combatlogger.handler.CombatPlayer;
import com.keurig.combatlogger.listeners.AttackListener;
import com.keurig.combatlogger.listeners.DeathListener;
import com.keurig.combatlogger.listeners.JoinListener;
import com.keurig.combatlogger.punishment.PunishmentManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CombatLogger extends JavaPlugin {

	@Getter
	private static CombatLogger instance;

	@Getter
	private CombatPlayer combatPlayer;

	@Getter
	private PunishmentManager punishmentManager;

	@Override
	public void onEnable() {
		instance = this;
		this.combatPlayer = new CombatPlayer(this);
		this.punishmentManager = new PunishmentManager();

		registerEvents();
		registerConfig();
	}

	@Override
	public void onDisable() {
		this.punishmentManager.unregisterPunishments();
	}

	public void registerEvents() {
		Bukkit.getPluginManager().registerEvents(new AttackListener(), this);
		Bukkit.getPluginManager().registerEvents(new JoinListener(), this);
		Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
	}

	public void registerConfig() {
		getConfig().options().copyDefaults();
		saveDefaultConfig();
	}

	/**
	 * @param player get the player tagged check
	 * @return true if user is tagged else false
	 * @deprecated moved api to API.isTagged(Player)
	 */
	public boolean isTagged(Player player) {
		return getCombatPlayer().getCombatLogged().containsKey(player.getUniqueId());
	}
}

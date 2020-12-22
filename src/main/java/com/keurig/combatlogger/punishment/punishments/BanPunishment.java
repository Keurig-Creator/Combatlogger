package com.keurig.combatlogger.punishment.punishments;

import com.keurig.combatlogger.punishment.Punishment;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPreLoginEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BanPunishment extends Punishment {

	private String[] args;

	private final Map<UUID, Long> banned;

	public BanPunishment() {
		super("BAN", 2);

		this.banned = new HashMap<>();
	}

	@EventHandler
	public void onPreJoin(PlayerPreLoginEvent event) {
		if (this.banned.containsKey(event.getUniqueId()) && this.banned.get(event.getUniqueId()) > System.currentTimeMillis())
			event.disallow(PlayerPreLoginEvent.Result.KICK_BANNED, ChatColor.translateAlternateColorCodes('&',
					this.args[1].replace("{timeRemaining}", timeFormat(this.banned.get(event.getUniqueId()) - System.currentTimeMillis()))));
	}

	@Override
	public void onQuit(String label, String[] args) {
		final Player player = getPlayer();

		if (player.hasPermission("combatlogger.admin"))
			return;

		this.args = args;
		this.banned.put(player.getUniqueId(), System.currentTimeMillis() + (Integer.parseInt(args[0]) * 1000));
	}

	private String timeFormat(long milis) {

		long second = (milis / 1000) % 60;
		long minute = (milis / (1000 * 60)) % 60;

		if (minute > 0) {
			return String.format("%dm %ds", minute, second);
		} else if (second > 0) {
			return String.format("%ds", second);
		} else
			return String.format("%dms", milis);
	}
}

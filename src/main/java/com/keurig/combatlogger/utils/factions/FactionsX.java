package com.keurig.combatlogger.utils.factions;


import net.prosavage.factionsx.command.engine.FCommand;
import net.prosavage.factionsx.core.FPlayer;
import net.prosavage.factionsx.manager.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FactionsX implements FactionsHook {

	@Override
	public void disableFlight(Player player) {
		FPlayer fPlayer = PlayerManager.INSTANCE.getFPlayer(player);
		fPlayer.setFFlying(false);
	}

	@Override
	public List<String> aliases() {

		List<String> aliases = new ArrayList<>();

		for (Map.Entry<String, Map<String, Object>> entry : Bukkit.getPluginManager().getPlugin("FactionsX").getDescription().getCommands().entrySet()) {
			String name = entry.getKey();
			Map<String, Object> command = entry.getValue();
			if (name.equals("factions")) {
				aliases.addAll((Collection<? extends String>) command.get("aliases"));
				aliases.add(name);
			}
		}

		return aliases;
	}

	@Override
	public List<String> subAliases() {

		List<String> subAliases = new ArrayList<>();
		
		for (FCommand subCommand : net.prosavage.factionsx.FactionsX.baseCommand.getSubCommands()) {
			subAliases.addAll(subCommand.getAliases());
		}

		return subAliases;
	}
}

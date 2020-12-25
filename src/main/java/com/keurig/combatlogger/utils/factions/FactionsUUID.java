package com.keurig.combatlogger.utils.factions;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.cmd.FCmdRoot;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FactionsUUID implements FactionsHook {

	@Override
	public void disableFlight(Player player) {
		cancelFFly(player);
	}

	@Override
	public List<String> aliases() {
		return new ArrayList<>(FCmdRoot.getInstance().aliases);
	}

	@Override
	public List<String> subAliases() {
		return new ArrayList<>(FCmdRoot.getInstance().cmdFly.aliases);
	}

	private void cancelFFly(Player player) {
		if (player == null) {
			return;
		}

		FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
		if (fPlayer.isFlying()) {
			fPlayer.setFlying(false, false);
			if (fPlayer.isAutoFlying()) {
				fPlayer.setAutoFlying(false);
			}
		}
	}
}

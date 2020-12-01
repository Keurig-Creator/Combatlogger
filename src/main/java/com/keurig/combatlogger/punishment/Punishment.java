package com.keurig.combatlogger.punishment;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

@Getter
@Setter
public abstract class Punishment implements Listener {

	/**
	 * Get the player who logged out during combat.
	 */
	private Player player;

	/**
	 * Get the name of the punishment used in config.yml use `UPPER CASE` format.
	 */
	private final String name;

	/**
	 * Get the amount of args required for the punishment, if none then not needed.
	 */
	private final int numberArgs;

	public Punishment(String name) {
		this(name, 0);
	}

	public Punishment(String name, int numberArgs) {
		this.name = name;
		this.numberArgs = numberArgs;
	}

	/**
	 * Run whenced the place quits the server during combat
	 *
	 * @param label the name of the punishment
	 * @param args  the arguments used
	 */
	public abstract void onQuit(String label, String[] args);

	@Override
	public String toString() {
		return "Punishment{" +
				"name='" + this.name + '\'' +
				'}';
	}
}

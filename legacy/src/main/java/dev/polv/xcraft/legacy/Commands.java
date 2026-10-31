package dev.polv.xcraft.legacy;

import java.util.Map;

/** /xcraft logic shared by the pre-1.13 command class and the 1.13 Brigadier registration. */
public final class Commands {
	public static final String USAGE = "/xcraft [ores|mobs|hostile|passive|xp|furnace] [value] | /xcraft <block|mob> [id] [value] | /xcraft reload";
	// Formatting codes; each command class turns them into its version's chat colours
	public static final char GOLD = '6';
	public static final char GREEN = 'a';
	public static final char RED = 'c';
	public static final char AQUA = 'b';
	public static final char GRAY = '7';

	/** Sends one line of feedback; {@code broadcast} tells operators (Brigadier only). */
	public interface Reply {
		void send(String message, char color, boolean broadcast);
	}

	private Commands() {
	}

	/** Runs the command; sends feedback through {@code reply}. */
	public static void run(String[] args, Reply reply) {
		if (args.length == 0) {
			reply.send("xCraft multipliers", GOLD, false);
			for (int i = 0; i < Config.MULTIPLIERS.length; i++) line(i, reply);
			for (int i = 0; i < Config.OVERRIDES.length; i++) if (!Config.overrides(i).isEmpty()) overrides(i, reply);
			return;
		}
		if (args.length == 1 && args[0].equals("reload")) {
			Config.reload();
			reply.send("xCraft config reloaded: " + Config.summary(), GREEN, true);
			return;
		}
		// Dev-only self test (-Dxcraft.debug=true): builds the scenarios in the world and reports the results
		if (args.length == 1 && args[0].equals("test") && LegacyXcraft.DEBUG) {
			reply.send(SelfTest.run(), GREEN, false);
			return;
		}
		int list = Config.overrideIndexOf(args[0]);
		if (list >= 0) {
			override(list, args, reply);
			return;
		}
		int index = Config.indexOf(args[0]);
		if (index < 0 || args.length > 2) {
			reply.send("Usage: " + USAGE, RED, false);
			return;
		}
		if (args.length == 1) {
			line(index, reply);
			return;
		}
		int value = parse(args[1]);
		if (value < Config.min(index) || value > Config.MAX) {
			reply.send("The value must be between " + Config.min(index) + " and " + Config.MAX, RED, false);
			return;
		}
		set(index, value, reply);
	}

	public static void set(int index, int value, Reply reply) {
		Config.set(index, value);
		reply.send("Set " + Config.MULTIPLIERS[index][0] + " multiplier to " + (value == 0 ? "0 (same as mobs)" : "x" + value), AQUA, true);
	}

	public static void line(int index, Reply reply) {
		int value = Config.get(index);
		reply.send(" " + Config.MULTIPLIERS[index][0] + ": " + (value == 0 ? "same as mobs" : "x" + value), value > 1 ? AQUA : GRAY, false);
	}

	/** /xcraft block|mob [id] [value]: lists the overrides, shows one, or sets one (0 removes it). */
	public static void override(int list, String[] args, Reply reply) {
		String name = Config.OVERRIDES[list][0];
		if (args.length == 1) {
			if (Config.overrides(list).isEmpty()) reply.send(" No " + name + " overrides", GRAY, false);
			else overrides(list, reply);
			return;
		}
		if (args.length > 3) {
			reply.send("Usage: /xcraft " + name + " [id] [0-" + Config.MAX + "] (0 removes the override)", RED, false);
			return;
		}
		String id = args[1];
		if (args.length == 2) {
			Integer value = Config.override(list, id);
			if (value == null) reply.send(" " + name + " " + id + ": no override", GRAY, false);
			else reply.send(" " + name + " " + id + ": x" + value, AQUA, false);
			return;
		}
		int value = parse(args[2]);
		if (value < 0 || value > Config.MAX) {
			reply.send("The value must be between 0 (remove) and " + Config.MAX, RED, false);
			return;
		}
		Config.setOverride(list, id, value);
		reply.send(value == 0 ? "Removed the " + name + " override for " + id : "Set " + name + " " + id + " multiplier to x" + value, AQUA, true);
	}

	private static void overrides(int list, Reply reply) {
		StringBuilder s = new StringBuilder(" " + Config.OVERRIDES[list][0] + ":");
		for (Map.Entry<String, Integer> entry : Config.overrides(list).entrySet()) {
			s.append(' ').append(entry.getKey()).append(" x").append(entry.getValue());
		}
		reply.send(s.toString(), AQUA, false);
	}

	private static int parse(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
}

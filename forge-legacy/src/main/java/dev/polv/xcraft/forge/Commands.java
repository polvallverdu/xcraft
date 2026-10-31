package dev.polv.xcraft.forge;

import java.util.Map;

/** /xcraft logic shared by the pre-1.13 command class and the Brigadier registration. */
public final class Commands {
	public static final String USAGE = "/xcraft [ores|mobs|hostile|passive|xp|furnace] [value] | /xcraft <block|mob> [id] [value] | /xcraft reload";
	// Section-sign colour codes: the one text format every version understands
	private static final String GOLD = "§6", GREEN = "§a", AQUA = "§b", RED = "§c", GRAY = "§7";

	private Commands() {
	}

	/** Where a command's feedback goes. */
	public interface Reply {
		void send(String message);
	}

	/** The loader-specific {@code /xcraft test} (see SelfTest); only used with -Dxcraft.debug=true. */
	public interface Test {
		String run();
	}

	public static Test selfTest;

	/** Runs the command; sends feedback through {@code reply}. */
	public static void run(String[] args, Reply reply) {
		if (args.length == 0) {
			reply.send(GOLD + "xCraft multipliers");
			for (int i = 0; i < Config.MULTIPLIERS.length; i++) reply.send(line(i));
			for (int i = 0; i < Config.OVERRIDES.length; i++) if (!Config.overrides(i).isEmpty()) reply.send(overrides(i));
			return;
		}
		if (args.length == 1 && args[0].equals("test") && Xcraft.DEBUG && selfTest != null) {
			reply.send(ServerThread.call(selfTest));
			return;
		}
		if (args.length == 1 && args[0].equals("reload")) {
			Config.reload();
			reply.send(GREEN + "xCraft config reloaded: " + Config.summary());
			return;
		}
		int list = Config.overrideIndexOf(args[0]);
		if (list >= 0) {
			runOverride(list, args, reply);
			return;
		}
		int index = Config.indexOf(args[0]);
		if (index < 0 || args.length > 2) {
			reply.send(RED + "Usage: " + USAGE);
			return;
		}
		if (args.length == 1) {
			reply.send(line(index));
			return;
		}
		int value = parse(args[1]);
		if (value < Config.min(index) || value > Config.MAX) {
			reply.send(RED + "The value must be between " + Config.min(index) + " and " + Config.MAX);
			return;
		}
		Config.set(index, value);
		reply.send(AQUA + "Set " + Config.MULTIPLIERS[index][0] + " multiplier to " + describe(value));
	}

	/** /xcraft block|mob [id] [value]: lists the overrides, shows one, or sets one (0 removes it). */
	private static void runOverride(int list, String[] args, Reply reply) {
		String name = Config.OVERRIDES[list][0];
		if (args.length == 1) {
			reply.send(Config.overrides(list).isEmpty() ? GRAY + " No " + name + " overrides" : overrides(list));
			return;
		}
		if (args.length > 3) {
			reply.send(RED + "Usage: /xcraft " + name + " [id] [0-" + Config.MAX + "] (0 removes the override)");
			return;
		}
		String id = args[1];
		if (args.length == 2) {
			Integer value = Config.override(list, id);
			reply.send(value == null ? GRAY + " " + name + " " + id + ": no override" : AQUA + " " + name + " " + id + ": x" + value);
			return;
		}
		int value = parse(args[2]);
		if (value < 0 || value > Config.MAX) {
			reply.send(RED + "The value must be between 0 (remove) and " + Config.MAX);
			return;
		}
		Config.setOverride(list, id, value);
		reply.send(AQUA + (value == 0 ? "Removed the " + name + " override for " + id : "Set " + name + " " + id + " multiplier to x" + value));
	}

	private static int parse(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private static String describe(int value) {
		return value == 0 ? "0 (same as mobs)" : "x" + value;
	}

	private static String line(int index) {
		int value = Config.get(index);
		return (value > 1 ? AQUA : GRAY) + " " + Config.MULTIPLIERS[index][0] + ": " + (value == 0 ? "same as mobs" : "x" + value);
	}

	private static String overrides(int list) {
		StringBuilder s = new StringBuilder(AQUA + " " + Config.OVERRIDES[list][0] + ":");
		for (Map.Entry<String, Integer> entry : Config.overrides(list).entrySet()) {
			s.append(' ').append(entry.getKey()).append(" x").append(entry.getValue());
		}
		return s.toString();
	}
}

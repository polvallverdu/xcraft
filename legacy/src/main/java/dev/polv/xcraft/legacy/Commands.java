package dev.polv.xcraft.legacy;

/** /xcraft logic shared by the pre-1.13 command class and the 1.13 Brigadier registration. */
public final class Commands {
	public static final String USAGE = "/xcraft [ores|mobs|xp|furnace] [1-1000] | /xcraft reload";
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
		int index = Config.indexOf(args[0]);
		if (index < 0 || args.length > 2) {
			reply.send("Usage: " + USAGE, RED, false);
			return;
		}
		if (args.length == 1) {
			line(index, reply);
			return;
		}
		int value;
		try {
			value = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			value = -1;
		}
		if (value < Config.MIN || value > Config.MAX) {
			reply.send("The value must be between " + Config.MIN + " and " + Config.MAX, RED, false);
			return;
		}
		set(index, value, reply);
	}

	public static void set(int index, int value, Reply reply) {
		Config.set(index, value);
		reply.send("Set " + Config.MULTIPLIERS[index][0] + " multiplier to x" + value, AQUA, true);
	}

	public static void line(int index, Reply reply) {
		int value = Config.get(index);
		reply.send(" " + Config.MULTIPLIERS[index][0] + ": x" + value, value > 1 ? AQUA : GRAY, false);
	}
}

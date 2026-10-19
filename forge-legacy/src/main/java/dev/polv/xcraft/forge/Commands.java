package dev.polv.xcraft.forge;

/** /xcraft logic shared by the pre-1.13 command class and the Brigadier registration. */
public final class Commands {
	public static final String USAGE = "/xcraft [ores|mobs|xp|furnace] [1-1000] | /xcraft reload";
	// Section-sign colour codes: the one text format every version understands
	private static final String GOLD = "§6", GREEN = "§a", AQUA = "§b", RED = "§c", GRAY = "§7";

	private Commands() {
	}

	/** Where a command's feedback goes. */
	public interface Reply {
		void send(String message);
	}

	/** Runs the command; sends feedback through {@code reply}. */
	public static void run(String[] args, Reply reply) {
		if (args.length == 0) {
			reply.send(GOLD + "xCraft multipliers");
			for (int i = 0; i < Config.MULTIPLIERS.length; i++) reply.send(line(i));
			return;
		}
		if (args.length == 1 && args[0].equals("reload")) {
			Config.reload();
			reply.send(GREEN + "xCraft config reloaded: " + Config.summary());
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
		int value;
		try {
			value = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			value = -1;
		}
		if (value < Config.MIN || value > Config.MAX) {
			reply.send(RED + "The value must be between " + Config.MIN + " and " + Config.MAX);
			return;
		}
		Config.set(index, value);
		reply.send(AQUA + "Set " + Config.MULTIPLIERS[index][0] + " multiplier to x" + value);
	}

	private static String line(int index) {
		int value = Config.get(index);
		return (value > 1 ? AQUA : GRAY) + " " + Config.MULTIPLIERS[index][0] + ": x" + value;
	}
}

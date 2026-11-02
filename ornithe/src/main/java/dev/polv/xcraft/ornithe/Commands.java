package dev.polv.xcraft.ornithe;

import net.minecraft.text.Formatting;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.function.Consumer;

/** /xcraft logic shared by the pre-1.13 command class and the 1.13 Brigadier registration. */
public final class Commands {
	public static final String USAGE = "/xcraft [ores|mobs|hostile|passive|xp|furnace] [value] | /xcraft <block|mob> [id] [value] | /xcraft reload";

	private Commands() {
	}

	public static Text text(String s, Formatting color) {
		Text text = new LiteralText(s);
		text.setStyle(new Style().setColor(color));
		return text;
	}

	/** Runs the command; sends feedback through {@code reply}. */
	public static void run(String[] args, Consumer<Text> reply) {
		if (args.length == 0) {
			reply.accept(text("xCraft multipliers", Formatting.GOLD));
			for (int i = 0; i < Config.MULTIPLIERS.length; i++) reply.accept(line(i));
			for (int i = 0; i < Config.OVERRIDES.length; i++) if (!Config.overrides(i).isEmpty()) reply.accept(overrides(i));
			return;
		}
		if (args.length == 1 && args[0].equals("reload")) {
			Config.reload();
			reply.accept(text("xCraft config reloaded: " + Config.summary(), Formatting.GREEN));
			return;
		}
		int list = Config.overrideIndexOf(args[0]);
		if (list >= 0) {
			override(list, args, reply);
			return;
		}
		int index = Config.indexOf(args[0]);
		if (index < 0 || args.length > 2) {
			reply.accept(text("Usage: " + USAGE, Formatting.RED));
			return;
		}
		if (args.length == 1) {
			reply.accept(line(index));
			return;
		}
		int value = parse(args[1]);
		if (value < Config.min(index) || value > Config.MAX) {
			reply.accept(text("The value must be between " + Config.min(index) + " and " + Config.MAX, Formatting.RED));
			return;
		}
		set(index, value, reply);
	}

	public static void set(int index, int value, Consumer<Text> reply) {
		Config.set(index, value);
		reply.accept(text("Set " + Config.MULTIPLIERS[index][0] + " multiplier to " + (value == 0 ? "0 (same as mobs)" : "x" + value), Formatting.AQUA));
	}

	public static Text line(int index) {
		int value = Config.get(index);
		return text(" " + Config.MULTIPLIERS[index][0] + ": " + (value == 0 ? "same as mobs" : "x" + value), value > 1 ? Formatting.AQUA : Formatting.GRAY);
	}

	/** /xcraft block|mob [id] [value]: lists the overrides, shows one, or sets one (0 removes it). */
	public static void override(int list, String[] args, Consumer<Text> reply) {
		String name = Config.OVERRIDES[list][0];
		if (args.length == 1) {
			reply.accept(Config.overrides(list).isEmpty() ? text(" No " + name + " overrides", Formatting.GRAY) : overrides(list));
			return;
		}
		if (args.length > 3) {
			reply.accept(text("Usage: /xcraft " + name + " [id] [0-" + Config.MAX + "] (0 removes the override)", Formatting.RED));
			return;
		}
		String id = args[1];
		if (args.length == 2) {
			Integer value = Config.override(list, id);
			reply.accept(value == null ? text(" " + name + " " + id + ": no override", Formatting.GRAY)
				: text(" " + name + " " + id + ": x" + value, Formatting.AQUA));
			return;
		}
		int value = parse(args[2]);
		if (value < 0 || value > Config.MAX) {
			reply.accept(text("The value must be between 0 (remove) and " + Config.MAX, Formatting.RED));
			return;
		}
		Config.setOverride(list, id, value);
		reply.accept(text(value == 0 ? "Removed the " + name + " override for " + id : "Set " + name + " " + id + " multiplier to x" + value, Formatting.AQUA));
	}

	private static Text overrides(int list) {
		StringBuilder s = new StringBuilder(" " + Config.OVERRIDES[list][0] + ":");
		for (Map.Entry<String, Integer> entry : Config.overrides(list).entrySet()) {
			s.append(' ').append(entry.getKey()).append(" x").append(entry.getValue());
		}
		return text(s.toString(), Formatting.AQUA);
	}

	private static int parse(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
}

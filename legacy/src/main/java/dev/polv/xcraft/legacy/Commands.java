package dev.polv.xcraft.legacy;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

/** /xcraft logic shared by the pre-1.13 command class and the 1.13 Brigadier registration. */
public final class Commands {
	public static final String USAGE = "/xcraft [ores|mobs|xp|furnace] [1-1000] | /xcraft reload";

	private Commands() {
	}

	public static Text text(String s, Formatting color) {
		Text text = new LiteralText(s);
		text.setStyle(new Style().setFormatting(color));
		return text;
	}

	/** Runs the command; sends feedback through {@code reply}. */
	public static void run(String[] args, Consumer<Text> reply) {
		if (args.length == 0) {
			reply.accept(text("xCraft multipliers", Formatting.GOLD));
			for (int i = 0; i < Config.MULTIPLIERS.length; i++) reply.accept(line(i));
			return;
		}
		if (args.length == 1 && args[0].equals("reload")) {
			Config.reload();
			reply.accept(text("xCraft config reloaded: " + Config.summary(), Formatting.GREEN));
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
		int value;
		try {
			value = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			value = -1;
		}
		if (value < Config.MIN || value > Config.MAX) {
			reply.accept(text("The value must be between " + Config.MIN + " and " + Config.MAX, Formatting.RED));
			return;
		}
		set(index, value, reply);
	}

	public static void set(int index, int value, Consumer<Text> reply) {
		Config.set(index, value);
		reply.accept(text("Set " + Config.MULTIPLIERS[index][0] + " multiplier to x" + value, Formatting.AQUA));
	}

	public static Text line(int index) {
		int value = Config.get(index);
		return text(" " + Config.MULTIPLIERS[index][0] + ": x" + value, value > 1 ? Formatting.AQUA : Formatting.GRAY);
	}
}

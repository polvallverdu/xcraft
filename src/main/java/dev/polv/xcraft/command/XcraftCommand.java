package dev.polv.xcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.polv.xcraft.config.Multiplier;
import dev.polv.xcraft.config.Overrides;
import dev.polv.xcraft.config.XcraftConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
//? if >=1.16 {
import net.minecraft.network.chat.MutableComponent;
//?}
//? if <1.19 {
/*import net.minecraft.network.chat.TextComponent;
*///?}

import java.util.Map;

/**
 * {@code /xcraft} shows all multipliers, {@code /xcraft <name> [value]} reads or sets one,
 * {@code /xcraft <block|mob> [<id> [value]]} lists, reads or sets (0 removes) per-id multipliers,
 * {@code /xcraft reload} re-reads the config file. Requires permission level 2 (operators).
 */
public final class XcraftCommand {
	private XcraftCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("xcraft")
			//? if >=1.21.11 {
			/*.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
			*///?} else {
			.requires(source -> source.hasPermission(2))
			//?}
			.executes(ctx -> {
				send(ctx.getSource(), text("xCraft multipliers").withStyle(ChatFormatting.GOLD), false);
				for (Multiplier m : Multiplier.values()) line(ctx.getSource(), m);
				for (Overrides list : Overrides.values()) {
					if (!XcraftConfig.overrides(list).isEmpty()) overrides(ctx.getSource(), list);
				}
				return 1;
			})
			.then(Commands.literal("reload").executes(ctx -> {
				XcraftConfig.reload();
				send(ctx.getSource(), text("xCraft config reloaded: " + XcraftConfig.summary()).withStyle(ChatFormatting.GREEN), true);
				return 1;
			}));

		for (Multiplier m : Multiplier.values()) root.then(multiplier(m));
		for (Overrides list : Overrides.values()) root.then(overrides(list));

		dispatcher.register(root);
	}

	private static LiteralArgumentBuilder<CommandSourceStack> multiplier(Multiplier m) {
		return Commands.literal(m.commandName)
			.executes(ctx -> {
				line(ctx.getSource(), m);
				return XcraftConfig.get(m);
			})
			.then(Commands.argument("value", IntegerArgumentType.integer(m.min, XcraftConfig.MAX)).executes(ctx -> {
				int value = IntegerArgumentType.getInteger(ctx, "value");
				XcraftConfig.set(m, value);
				send(ctx.getSource(), text("Set " + m.commandName + " multiplier to ")
					.append(text(describe(value)).withStyle(ChatFormatting.AQUA)), true);
				return value;
			}));
	}

	/** {@code /xcraft block|mob [<id> [value]]}. Ids contain ':', which Brigadier's word arguments reject: the rest of the line is parsed here. */
	private static LiteralArgumentBuilder<CommandSourceStack> overrides(Overrides list) {
		return Commands.literal(list.commandName)
			.executes(ctx -> {
				if (XcraftConfig.overrides(list).isEmpty()) {
					send(ctx.getSource(), text(" No " + list.commandName + " overrides").withStyle(ChatFormatting.GRAY), false);
				} else {
					overrides(ctx.getSource(), list);
				}
				return XcraftConfig.overrides(list).size();
			})
			.then(Commands.argument("id", StringArgumentType.greedyString()).executes(ctx -> {
				CommandSourceStack source = ctx.getSource();
				String[] args = StringArgumentType.getString(ctx, "id").trim().split("\\s+");
				String id = args[0];
				if (args.length == 1) {
					Integer value = XcraftConfig.override(list, id);
					send(source, text(" " + list.commandName + " " + id + ": ").withStyle(ChatFormatting.GRAY)
						.append(value == null ? text("no override").withStyle(ChatFormatting.WHITE) : text("x" + value).withStyle(ChatFormatting.AQUA)), false);
					return value == null ? 0 : value;
				}
				int value;
				try {
					value = args.length == 2 ? Integer.parseInt(args[1]) : -1;
				} catch (NumberFormatException e) {
					value = -1;
				}
				if (value < 0 || value > XcraftConfig.MAX) {
					send(source, text("Usage: /xcraft " + list.commandName + " [<id> [0-" + XcraftConfig.MAX + "]] (0 removes the override)")
						.withStyle(ChatFormatting.RED), false);
					return 0;
				}
				XcraftConfig.setOverride(list, id, value);
				if (value == 0) {
					send(source, text("Removed the " + list.commandName + " override for " + id), true);
				} else {
					send(source, text("Set " + list.commandName + " " + id + " multiplier to ")
						.append(text("x" + value).withStyle(ChatFormatting.AQUA)), true);
				}
				return value;
			}));
	}

	private static String describe(int value) {
		return value == 0 ? "0 (same as mobs)" : "x" + value;
	}

	private static void line(CommandSourceStack source, Multiplier m) {
		int value = XcraftConfig.get(m);
		send(source, text(" " + m.commandName + ": ").withStyle(ChatFormatting.GRAY)
			.append(text(value == 0 ? "same as mobs" : "x" + value).withStyle(value > 1 ? ChatFormatting.AQUA : ChatFormatting.WHITE)), false);
	}

	private static void overrides(CommandSourceStack source, Overrides list) {
		StringBuilder s = new StringBuilder();
		for (Map.Entry<String, Integer> entry : XcraftConfig.overrides(list).entrySet()) {
			s.append(' ').append(entry.getKey()).append(" x").append(entry.getValue());
		}
		send(source, text(" " + list.commandName + ":").withStyle(ChatFormatting.GRAY)
			.append(text(s.toString()).withStyle(ChatFormatting.AQUA)), false);
	}

	// MutableComponent arrived in 1.16; before that withStyle/append live on Component itself
	//? if >=1.16 {
	private static MutableComponent text(String s) {
	//?} else {
	/*private static Component text(String s) {
	*///?}
		//? if >=1.19 {
		return Component.literal(s);
		//?} else {
		/*return new TextComponent(s);
		*///?}
	}

	private static void send(CommandSourceStack source, Component text, boolean broadcast) {
		//? if >=1.20 {
		source.sendSuccess(() -> text, broadcast);
		//?} else {
		/*source.sendSuccess(text, broadcast);
		*///?}
	}
}

package dev.polv.xcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.polv.xcraft.config.Multiplier;
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

/**
 * {@code /xcraft} shows all multipliers, {@code /xcraft <name> [value]} reads or sets one,
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
				return 1;
			})
			.then(Commands.literal("reload").executes(ctx -> {
				XcraftConfig.reload();
				send(ctx.getSource(), text("xCraft config reloaded: " + XcraftConfig.summary()).withStyle(ChatFormatting.GREEN), true);
				return 1;
			}));

		for (Multiplier m : Multiplier.values()) root.then(multiplier(m));

		dispatcher.register(root);
	}

	private static LiteralArgumentBuilder<CommandSourceStack> multiplier(Multiplier m) {
		return Commands.literal(m.commandName)
			.executes(ctx -> {
				line(ctx.getSource(), m);
				return XcraftConfig.get(m);
			})
			.then(Commands.argument("value", IntegerArgumentType.integer(XcraftConfig.MIN, XcraftConfig.MAX)).executes(ctx -> {
				int value = IntegerArgumentType.getInteger(ctx, "value");
				XcraftConfig.set(m, value);
				send(ctx.getSource(), text("Set " + m.commandName + " multiplier to ")
					.append(text("x" + value).withStyle(ChatFormatting.AQUA)), true);
				return value;
			}));
	}

	private static void line(CommandSourceStack source, Multiplier m) {
		int value = XcraftConfig.get(m);
		send(source, text(" " + m.commandName + ": ").withStyle(ChatFormatting.GRAY)
			.append(text("x" + value).withStyle(value > 1 ? ChatFormatting.AQUA : ChatFormatting.WHITE)), false);
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

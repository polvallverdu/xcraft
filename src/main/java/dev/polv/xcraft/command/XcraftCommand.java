package dev.polv.xcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.polv.xcraft.config.XcraftConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

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
				ctx.getSource().sendSuccess(() -> Component.literal("xCraft multipliers").withStyle(ChatFormatting.GOLD), false);
				line(ctx.getSource(), "ores", XcraftConfig.oreDropMultiplier);
				line(ctx.getSource(), "mobs", XcraftConfig.entityDropMultiplier);
				line(ctx.getSource(), "xp", XcraftConfig.xpMultiplier);
				line(ctx.getSource(), "furnace", XcraftConfig.furnaceSpeedMultiplier);
				return 1;
			})
			.then(Commands.literal("reload").executes(ctx -> {
				XcraftConfig.load();
				ctx.getSource().sendSuccess(() -> Component.literal("xCraft config reloaded: " + XcraftConfig.summary()).withStyle(ChatFormatting.GREEN), true);
				return 1;
			}));

		root.then(multiplier("ores", () -> XcraftConfig.oreDropMultiplier, v -> XcraftConfig.oreDropMultiplier = v));
		root.then(multiplier("mobs", () -> XcraftConfig.entityDropMultiplier, v -> XcraftConfig.entityDropMultiplier = v));
		root.then(multiplier("xp", () -> XcraftConfig.xpMultiplier, v -> XcraftConfig.xpMultiplier = v));
		root.then(multiplier("furnace", () -> XcraftConfig.furnaceSpeedMultiplier, v -> XcraftConfig.furnaceSpeedMultiplier = v));

		dispatcher.register(root);
	}

	private static LiteralArgumentBuilder<CommandSourceStack> multiplier(String name, IntSupplier getter, IntConsumer setter) {
		return Commands.literal(name)
			.executes(ctx -> {
				line(ctx.getSource(), name, getter.getAsInt());
				return getter.getAsInt();
			})
			.then(Commands.argument("value", IntegerArgumentType.integer(XcraftConfig.MIN, XcraftConfig.MAX)).executes(ctx -> {
				int value = IntegerArgumentType.getInteger(ctx, "value");
				setter.accept(value);
				XcraftConfig.save();
				ctx.getSource().sendSuccess(() -> Component.literal("Set " + name + " multiplier to ")
					.append(Component.literal("x" + value).withStyle(ChatFormatting.AQUA)), true);
				return value;
			}));
	}

	private static void line(CommandSourceStack source, String name, int value) {
		MutableComponent text = Component.literal(" " + name + ": ").withStyle(ChatFormatting.GRAY)
			.append(Component.literal("x" + value).withStyle(value > 1 ? ChatFormatting.AQUA : ChatFormatting.WHITE));
		source.sendSuccess(() -> text, false);
	}
}

package dev.polv.xcraft.forge.ml;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.polv.xcraft.forge.Commands;
import dev.polv.xcraft.forge.Config;
import net.minecraft.command.CommandSource;
//? if >=1.14 {
/*import net.minecraft.util.text.StringTextComponent;
*///?} else {
import net.minecraft.util.text.TextComponentString;
//?}

/** /xcraft on Forge 1.13.2 - 1.16.5 (Brigadier). Operators only (permission level 2). */
public final class BrigadierCommand {
	private BrigadierCommand() {
	}

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> root = net.minecraft.command.Commands.literal("xcraft");
		root.requires(source -> source.hasPermissionLevel(2))
			.executes(ctx -> run(ctx.getSource(), new String[0]))
			.then(net.minecraft.command.Commands.literal("reload").executes(ctx -> run(ctx.getSource(), new String[]{"reload"})));
		for (String[] multiplier : Config.MULTIPLIERS) {
			String name = multiplier[0];
			root.then(net.minecraft.command.Commands.literal(name)
				.executes(ctx -> run(ctx.getSource(), new String[]{name}))
				.then(net.minecraft.command.Commands.argument("value", IntegerArgumentType.integer(Config.MIN, Config.MAX))
					.executes(ctx -> run(ctx.getSource(), new String[]{name, String.valueOf(IntegerArgumentType.getInteger(ctx, "value"))}))));
		}
		dispatcher.register(root);
	}

	private static int run(CommandSource source, String[] args) {
		Commands.run(args, message -> {
			//? if >=1.14 {
			/*source.sendFeedback(new StringTextComponent(message), true);
			*///?} else {
			source.sendFeedback(new TextComponentString(message), true);
			//?}
		});
		return 1;
	}
}

package dev.polv.xcraft.forge.ml;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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
			.then(net.minecraft.command.Commands.literal("reload").executes(ctx -> run(ctx.getSource(), new String[]{"reload"})))
			// Dev-only self-test for the CI server test (Commands ignores it without -Dxcraft.debug=true)
			.then(net.minecraft.command.Commands.literal("test").executes(ctx -> run(ctx.getSource(), new String[]{"test"})));
		for (int i = 0; i < Config.MULTIPLIERS.length; i++) {
			String name = Config.MULTIPLIERS[i][0];
			root.then(net.minecraft.command.Commands.literal(name)
				.executes(ctx -> run(ctx.getSource(), new String[]{name}))
				.then(net.minecraft.command.Commands.argument("value", IntegerArgumentType.integer(Config.min(i), Config.MAX))
					.executes(ctx -> run(ctx.getSource(), new String[]{name, String.valueOf(IntegerArgumentType.getInteger(ctx, "value"))}))));
		}
		// /xcraft block|mob [<id> [value]]: ids contain ':', which Brigadier's word arguments reject, so take the rest
		for (String[] list : Config.OVERRIDES) {
			String name = list[0];
			root.then(net.minecraft.command.Commands.literal(name)
				.executes(ctx -> run(ctx.getSource(), new String[]{name}))
				.then(net.minecraft.command.Commands.argument("id", StringArgumentType.greedyString())
					.executes(ctx -> run(ctx.getSource(), (name + " " + StringArgumentType.getString(ctx, "id")).trim().split("\\s+")))));
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

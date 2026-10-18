package dev.polv.xcraft.ornithe;

//? if >=1.13 {
/*import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.handler.CommandManager;
import net.minecraft.server.command.source.CommandSourceStack;

// /xcraft on 1.13 (Brigadier)
public final class BrigadierCommand {
	private BrigadierCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> root = CommandManager.literal("xcraft");
		root.requires(source -> source.hasPermissions(2))
			.executes(ctx -> {
				Commands.run(new String[0], text -> ctx.getSource().sendSuccess(text, false));
				return 1;
			})
			.then(CommandManager.literal("reload").executes(ctx -> {
				Commands.run(new String[]{"reload"}, text -> ctx.getSource().sendSuccess(text, true));
				return 1;
			}));
		for (String[] multiplier : Config.MULTIPLIERS) {
			int index = Config.indexOf(multiplier[0]);
			root.then(CommandManager.literal(multiplier[0])
				.executes(ctx -> {
					ctx.getSource().sendSuccess(Commands.line(index), false);
					return Config.get(index);
				})
				.then(CommandManager.argument("value", IntegerArgumentType.integer(Config.MIN, Config.MAX)).executes(ctx -> {
					int value = IntegerArgumentType.getInteger(ctx, "value");
					Commands.set(index, value, text -> ctx.getSource().sendSuccess(text, true));
					return value;
				})));
		}
		dispatcher.register(root);
	}
}
*///?} else {
// Before 1.13 commands use XcraftCommand
public final class BrigadierCommand {
}
//?}

package dev.polv.xcraft.legacy;

//? if >=1.13 {
/*import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.class_3915;
import net.minecraft.server.command.CommandManager;

/^* /xcraft on 1.13 (Brigadier). class_3915 is ServerCommandSource, which Legacy Yarn leaves unnamed. ^/
public final class BrigadierCommand {
	private BrigadierCommand() {
	}

	public static void register(CommandDispatcher<class_3915> dispatcher) {
		LiteralArgumentBuilder<class_3915> root = CommandManager.method_17529("xcraft");
		root.requires(source -> source.method_17575(2))
			.executes(ctx -> {
				Commands.run(new String[0], text -> ctx.getSource().method_17459(text, false));
				return 1;
			})
			.then(CommandManager.method_17529("reload").executes(ctx -> {
				Commands.run(new String[]{"reload"}, text -> ctx.getSource().method_17459(text, true));
				return 1;
			}));
		for (String[] multiplier : Config.MULTIPLIERS) {
			int index = Config.indexOf(multiplier[0]);
			root.then(CommandManager.method_17529(multiplier[0])
				.executes(ctx -> {
					ctx.getSource().method_17459(Commands.line(index), false);
					return Config.get(index);
				})
				.then(CommandManager.method_17530("value", IntegerArgumentType.integer(Config.MIN, Config.MAX)).executes(ctx -> {
					int value = IntegerArgumentType.getInteger(ctx, "value");
					Commands.set(index, value, text -> ctx.getSource().method_17459(text, true));
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

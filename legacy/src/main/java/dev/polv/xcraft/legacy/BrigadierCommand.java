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
				Commands.run(new String[0], reply(ctx.getSource()));
				return 1;
			})
			.then(CommandManager.method_17529("reload").executes(ctx -> {
				Commands.run(new String[]{"reload"}, reply(ctx.getSource()));
				return 1;
			}));
		for (String[] multiplier : Config.MULTIPLIERS) {
			int index = Config.indexOf(multiplier[0]);
			root.then(CommandManager.method_17529(multiplier[0])
				.executes(ctx -> {
					Commands.line(index, reply(ctx.getSource()));
					return Config.get(index);
				})
				.then(CommandManager.method_17530("value", IntegerArgumentType.integer(Config.MIN, Config.MAX)).executes(ctx -> {
					int value = IntegerArgumentType.getInteger(ctx, "value");
					Commands.set(index, value, reply(ctx.getSource()));
					return value;
				})));
		}
		dispatcher.register(root);
	}

	private static Commands.Reply reply(class_3915 source) {
		return (message, color, broadcast) -> source.method_17459(Chat.text(message, color), broadcast);
	}
}
*///?} else {
// Before 1.13 commands use XcraftCommand
public final class BrigadierCommand {
}
//?}

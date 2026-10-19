package dev.polv.xcraft.legacy;

// /xcraft before 1.13 (pre-Brigadier command API). Operators only (permission level 2).
//? if >=1.13 {
/*// 1.13 uses Brigadier: see BrigadierCommand
public final class XcraftCommand {
}
*///?} else if >=1.9 {
import net.minecraft.command.AbstractCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;

public class XcraftCommand extends AbstractCommand {

	@Override
	public String getCommandName() {
		return "xcraft";
	}

	@Override
	public String getUsageTranslationKey(CommandSource source) {
		return Commands.USAGE;
	}

	@Override
	public int getPermissionLevel() {
		return 2;
	}

	@Override
	public void method_3279(MinecraftServer server, CommandSource source, String[] args) throws CommandException {
		Commands.run(args, (message, color, broadcast) -> Chat.send(source, message, color));
	}
}
//?} else if >=1.8 {
/*import net.minecraft.command.AbstractCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;

public class XcraftCommand extends AbstractCommand {

	@Override
	public String getCommandName() {
		return "xcraft";
	}

	@Override
	public String getUsageTranslationKey(CommandSource source) {
		return Commands.USAGE;
	}

	@Override
	public int getPermissionLevel() {
		return 2;
	}

	@Override
	public void execute(CommandSource source, String[] args) throws CommandException {
		Commands.run(args, (message, color, broadcast) -> Chat.send(source, message, color));
	}
}
*///?} else {
/*import net.minecraft.command.AbstractCommand;
import net.minecraft.command.CommandSource;

public class XcraftCommand extends AbstractCommand {

	@Override
	public String getCommandName() {
		return "xcraft";
	}

	@Override
	public String getUsageTranslationKey(CommandSource source) {
		return Commands.USAGE;
	}

	//? if >=1.4 {
	@Override
	public int getPermissionLevel() {
		return 2;
	}
	//?}
	// 1.3 has no permission levels: commands are for operators (and the console/RCON)

	@Override
	public void execute(CommandSource source, String[] args) {
		Commands.run(args, (message, color, broadcast) -> Chat.send(source, message, color));
	}

	// 1.7.10 and older: Command extends the raw Comparable
	@Override
	public int compareTo(Object other) {
		return getCommandName().compareTo(((net.minecraft.command.Command) other).getCommandName());
	}
}
*///?}

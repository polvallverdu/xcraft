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
		Commands.run(args, source::sendMessage);
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
		Commands.run(args, source::sendMessage);
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

	@Override
	public int getPermissionLevel() {
		return 2;
	}

	@Override
	public void execute(CommandSource source, String[] args) {
		Commands.run(args, source::sendMessage);
	}

	// 1.7.10's Command extends the raw Comparable
	@Override
	public int compareTo(Object other) {
		return getCommandName().compareTo(((net.minecraft.command.Command) other).getCommandName());
	}
}
*///?}

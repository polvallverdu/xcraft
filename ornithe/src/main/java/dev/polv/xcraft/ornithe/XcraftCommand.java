package dev.polv.xcraft.ornithe;

// /xcraft before 1.13 (pre-Brigadier command API). Operators only (permission level 2).
//? if >=1.13 {
/*// 1.13 uses Brigadier: see BrigadierCommand
public final class XcraftCommand {
}
*///?} else {
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.AbstractCommand;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;

public class XcraftCommand extends AbstractCommand {

	@Override
	public String getName() {
		return "xcraft";
	}

	@Override
	public String getUsage(CommandSource source) {
		return Commands.USAGE;
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void run(MinecraftServer server, CommandSource source, String[] args) throws CommandException {
		Commands.run(args, source::sendMessage);
	}
}
//?}

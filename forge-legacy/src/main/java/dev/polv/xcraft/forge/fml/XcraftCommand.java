package dev.polv.xcraft.forge.fml;

import dev.polv.xcraft.forge.Commands;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
//? if >=1.9 {
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
//?} else if >=1.8 {
/*import net.minecraft.command.CommandException;
import net.minecraft.util.ChatComponentText;
*///?} else {
/*import net.minecraft.util.ChatComponentText;
*///?}

/** /xcraft for Forge 1.7.2 - 1.12.2. Operators only (permission level 2). */
public class XcraftCommand extends CommandBase {

	//? if >=1.10 {
	@Override
	public String getName() {
		return "xcraft";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return Commands.USAGE;
	}
	//?} else {
	/*@Override
	public String getCommandName() {
		return "xcraft";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return Commands.USAGE;
	}
	*///?}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	//? if >=1.9 {
	@Override
	public void execute(MinecraftServer server, final ICommandSender sender, String[] args) throws CommandException {
		Commands.run(args, new Commands.Reply() {
			@Override
			public void send(String message) {
				reply(sender, message);
			}
		});
	}
	//?} else if >=1.8 {
	/*@Override
	public void processCommand(final ICommandSender sender, String[] args) throws CommandException {
		Commands.run(args, new Commands.Reply() {
			@Override
			public void send(String message) {
				reply(sender, message);
			}
		});
	}
	*///?} else {
	/*@Override
	public void processCommand(final ICommandSender sender, String[] args) {
		Commands.run(args, new Commands.Reply() {
			@Override
			public void send(String message) {
				reply(sender, message);
			}
		});
	}
	*///?}

	private static void reply(ICommandSender sender, String message) {
		//? if >=1.10 {
		sender.sendMessage(new TextComponentString(message));
		//?} else if >=1.9 {
		/*sender.addChatMessage(new TextComponentString(message));
		*///?} else {
		/*sender.addChatMessage(new ChatComponentText(message));
		*///?}
	}
}

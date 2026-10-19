package dev.polv.xcraft.forge.ancient;

import dev.polv.xcraft.forge.Commands;
//? if <1.4.5 {
/*import net.minecraft.src.*;
*///?} else {
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
//?}
//? if >=1.6 {
import net.minecraft.util.ChatMessageComponent;
//?}

/** /xcraft for Forge 1.3.2 - 1.6.4. Operators only (1.3.2 has no permission levels: CommandBase checks for op). */
public class XcraftCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "xcraft";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return Commands.USAGE;
	}

	//? if >=1.4.2 {
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	//?}

	// ICommand extends the raw Comparable
	@Override
	public int compareTo(Object other) {
		return getCommandName().compareTo(((ICommand) other).getCommandName());
	}

	@Override
	public void processCommand(final ICommandSender sender, String[] args) {
		Commands.run(args, new Commands.Reply() {
			@Override
			public void send(String message) {
				//? if >=1.6 {
				sender.sendChatToPlayer(ChatMessageComponent.createFromText(message));
				//?} else {
				/*sender.sendChatToPlayer(message);
				*///?}
			}
		});
	}
}

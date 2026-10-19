package dev.polv.xcraft.mixin;

import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
//? if fabric {
import com.mojang.brigadier.CommandDispatcher;
import dev.polv.xcraft.Xcraft;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Registers /xcraft on Fabric without Fabric API: older Fabric API builds lack the command module, and this is
 * all xCraft needed it for. (Forge and NeoForge use their RegisterCommandsEvent instead.)
 */
@Mixin(Commands.class)
public abstract class CommandsMixin {
	@Shadow
	@Final
	private CommandDispatcher<CommandSourceStack> dispatcher;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void xcraft$registerCommands(CallbackInfo ci) {
		Xcraft.registerCommands(dispatcher);
	}
}
//?} else {
/*// Forge and NeoForge register /xcraft through RegisterCommandsEvent
@Mixin(Commands.class)
public abstract class CommandsMixin {
}
*///?}

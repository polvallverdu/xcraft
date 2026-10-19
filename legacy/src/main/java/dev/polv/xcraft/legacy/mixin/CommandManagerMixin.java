package dev.polv.xcraft.legacy.mixin;

import net.minecraft.server.command.CommandManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=1.13 {
/*import com.mojang.brigadier.CommandDispatcher;
import dev.polv.xcraft.legacy.BrigadierCommand;
import net.minecraft.class_3915;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
*///?} else {
import dev.polv.xcraft.legacy.XcraftCommand;
import net.minecraft.server.command.CommandRegistry;
//?}

/** Registers /xcraft with the server's command manager (also covers singleplayer). */
@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {

	//? if >=1.13 {
	/*@Shadow
	@Final
	private CommandDispatcher<class_3915> field_19323;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void xcraft$register(CallbackInfo ci) {
		BrigadierCommand.register(field_19323);
	}
	*///?} else {
	@Inject(method = "<init>", at = @At("TAIL"))
	private void xcraft$register(CallbackInfo ci) {
		((CommandRegistry) (Object) this).registerCommand(new XcraftCommand());
	}
	//?}
}

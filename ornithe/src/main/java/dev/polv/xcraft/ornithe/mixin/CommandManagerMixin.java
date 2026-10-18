package dev.polv.xcraft.ornithe.mixin;

import net.minecraft.server.command.handler.CommandManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=1.13 {
/*import com.mojang.brigadier.CommandDispatcher;
import dev.polv.xcraft.ornithe.BrigadierCommand;
import net.minecraft.server.command.source.CommandSourceStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
*///?} else {
import dev.polv.xcraft.ornithe.XcraftCommand;
import net.minecraft.server.command.handler.CommandRegistry;
//?}

/** Registers /xcraft with the server's command manager (also covers singleplayer). */
@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {

	//? if >=1.13 {
	/*@Shadow
	@Final
	private CommandDispatcher<CommandSourceStack> dispatcher;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void xcraft$register(CallbackInfo ci) {
		BrigadierCommand.register(dispatcher);
	}
	*///?} else {
	@Inject(method = "<init>", at = @At("TAIL"))
	private void xcraft$register(CallbackInfo ci) {
		((CommandRegistry) (Object) this).register(new XcraftCommand());
	}
	//?}
}

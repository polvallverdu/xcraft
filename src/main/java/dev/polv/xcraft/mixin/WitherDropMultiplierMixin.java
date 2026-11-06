package dev.polv.xcraft.mixin;

import dev.polv.xcraft.MultiplierUtil;
//? if >=1.21.2 {
/*import net.minecraft.server.level.ServerLevel;
*///?}
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The Wither's nether star is not in its loot table: WitherBoss.dropCustomDeathLoot spawns it directly, so
 * EntityDropMultiplierMixin never sees it. Drop the missing (multiplier - 1) stars after the vanilla one.
 */
@Mixin(WitherBoss.class)
public abstract class WitherDropMultiplierMixin {

	// By name only: its parameters changed over the versions, and WitherBoss declares it once
	@Inject(method = "dropCustomDeathLoot", at = @At("TAIL"))
	private void xcraft$multiplyNetherStar(CallbackInfo ci) {
		WitherBoss self = (WitherBoss) (Object) this;
		int multiplier = MultiplierUtil.mobMultiplier(self);
		if (multiplier <= 1) return;
		MultiplierUtil.multiply(new ItemStack(Items.NETHER_STAR), multiplier - 1, stack -> {
			//? if >=1.21.2 {
			/*ItemEntity item = self.spawnAtLocation((ServerLevel) self.level(), stack);
			*///?} else {
			ItemEntity item = self.spawnAtLocation(stack);
			//?}
			// Like the vanilla star: never despawns
			if (item != null) item.setExtendedLifetime();
		});
	}
}

package dev.polv.xcraft.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import dev.polv.xcraft.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public class FurnaceSpeedMultiplierMixin {

    @Inject(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;canBurn(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/crafting/Recipe;Lnet/minecraft/core/NonNullList;I)Z", ordinal = 1, shift = At.Shift.AFTER))
    private static void injectCookingProgress(Level level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci, @Local(name = "bl2") LocalBooleanRef bl2) {
        AbstractFurnaceBlockEntityAccessor blockEntityAccessor = (AbstractFurnaceBlockEntityAccessor) blockEntity;

        boolean bl3 = !(blockEntityAccessor.getItems().get(0)).isEmpty();

        Recipe<?> recipe;
        if (bl3) {
            recipe = blockEntityAccessor.getQuickCheck().getRecipeFor(blockEntity, level).orElse(null);
        } else {
            recipe = null;
        }

        if (!AbstractFurnaceBlockEntityAccessor.canBurn(level.registryAccess(), recipe, blockEntityAccessor.getItems(), blockEntity.getMaxStackSize())) return;

        int i = 0;

        while (i < ModConfig.furnaceSpeedMultiplier) {
            blockEntityAccessor.setCookingProgress(blockEntityAccessor.getCookingProgress() + 1);
            if (blockEntityAccessor.getCookingProgress() == blockEntityAccessor.getCookingTotalTime()) {
                blockEntityAccessor.setCookingProgress(0);
                blockEntityAccessor.setCookingTotalTime(AbstractFurnaceBlockEntityAccessor.getTotalCookTime(level, blockEntity));
                if (AbstractFurnaceBlockEntityAccessor.burn(level.registryAccess(), recipe, blockEntityAccessor.getItems(), i)) {
                    blockEntity.setRecipeUsed(recipe);
                }

                bl2.set(true);
            }
            i++;
        }

        // we're removing one, because one will be added by the original method
        blockEntityAccessor.setCookingProgress(blockEntityAccessor.getCookingProgress() - 1);
    }

    @Redirect(method = "serverTick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;cookingProgress:I", opcode = Opcodes.GETFIELD, ordinal = 1))
    private static int injected(AbstractFurnaceBlockEntity instance) {
        AbstractFurnaceBlockEntityAccessor blockEntityAccessor = (AbstractFurnaceBlockEntityAccessor) instance;
        return blockEntityAccessor.getCookingTotalTime()+1;
    }
}

package dev.pov.xcraft.mixin;

import dev.pov.xcraft.config.ModConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Consumer;

@Mixin(LivingEntity.class)
public class EntityDropMultiplierMixin {

    @ModifyArg(method = "dropFromLootTable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;JLjava/util/function/Consumer;)V"))
    private Consumer<ItemStack> injected(Consumer<ItemStack> output) {
        return itemStack -> {
            itemStack.setCount(itemStack.getCount()* ModConfig.entityDropMultiplier);
            output.accept(itemStack);
        };
    }

}

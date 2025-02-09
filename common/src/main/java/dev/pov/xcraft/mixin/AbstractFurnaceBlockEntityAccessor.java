package dev.pov.xcraft.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityAccessor {

    @Accessor
    int getCookingProgress();

    @Accessor("cookingProgress")
    void setCookingProgress(int cookingProgress);

    @Accessor
    int getCookingTotalTime();

    @Accessor("cookingTotalTime")
    void setCookingTotalTime(int cookingTotalTime);

    @Accessor
    NonNullList<ItemStack> getItems();

    @Accessor
    RecipeManager.CachedCheck<Container, ? extends AbstractCookingRecipe> getQuickCheck();

    @Invoker("canBurn")
    static boolean canBurn(RegistryAccess registryAccess, @Nullable Recipe<?> recipe, NonNullList<ItemStack> inventory, int maxStackSize) {
        throw new AssertionError();
    }

    @Invoker("burn")
    static boolean burn(RegistryAccess registryAccess, @Nullable Recipe<?> recipe, NonNullList<ItemStack> inventory, int maxStackSize) {
        throw new AssertionError();
    }

    @Invoker("getTotalCookTime")
    static int getTotalCookTime(Level level, AbstractFurnaceBlockEntity blockEntity) {
        throw new AssertionError();
    }

}

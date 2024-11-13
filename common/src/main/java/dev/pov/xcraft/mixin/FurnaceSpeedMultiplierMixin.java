package dev.pov.xcraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public class FurnaceSpeedMultiplierMixin extends BaseContainerBlockEntity {


    protected FurnaceSpeedMultiplierMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Unique
    private static boolean xcraft$isLit(AbstractFurnaceBlockEntity blockEntity) {
        AbstractFurnaceBlockEntityAccessor accessor = (AbstractFurnaceBlockEntityAccessor) blockEntity;

        return accessor.getLitTime() > 0;
    }

    @Inject(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;isLit()Z", ordinal = 6), cancellable = true)
    private static void onProcessMultipleTimes(Level level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
        ci.cancel();

        AbstractFurnaceBlockEntityAccessor accessor = (AbstractFurnaceBlockEntityAccessor) blockEntity;

        Recipe recipe = accessor.getQuickCheck().getRecipeFor(blockEntity, level).orElse(null);
        int i = blockEntity.getMaxStackSize();

        if (xcraft$isLit(blockEntity) && AbstractFurnaceBlockEntityAccessor.canBurn(level.registryAccess(), recipe, accessor.getItems(), i)) {
            accessor.setCookingProgress(accessor.getCookingProgress()+1);
            if (accessor.getCookingProgress() == accessor.getCookingTotalTime()) {
                accessor.setCookingProgress(0);
                accessor.setCookingTotalTime(AbstractFurnaceBlockEntityAccessor.getTotalCookTime(level, blockEntity));
                if (AbstractFurnaceBlockEntityAccessor.burn(level.registryAccess(), recipe, accessor.getItems(), i)) {
                    blockEntity.setRecipeUsed(recipe);
                }
            }
        } else {
            accessor.setCookingProgress(0);
        }

        state = (BlockState)state.setValue(AbstractFurnaceBlock.LIT, xcraft$isLit(blockEntity));
        level.setBlock(pos, state, 3);

        setChanged(level, pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return null;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return null;
    }

    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getItem(int slot) {
        return null;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return null;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return null;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {

    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {

    }
}

package dev.polv.xcraft.mixin;

import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityAccessor {

	//? if >=1.21.4 {
	/*@Accessor("cookingTimer")
	*///?} else {
	@Accessor("cookingProgress")
	//?}
	int xcraft$getCookingProgress();

	//? if >=1.21.4 {
	/*@Accessor("cookingTimer")
	*///?} else {
	@Accessor("cookingProgress")
	//?}
	void xcraft$setCookingProgress(int value);

	@Accessor("cookingTotalTime")
	int xcraft$getCookingTotalTime();

	//? if >=1.21.4 {
	/*@Accessor("litTimeRemaining")
	*///?} else {
	@Accessor("litTime")
	//?}
	int xcraft$getLitTime();
}

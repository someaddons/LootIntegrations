package com.lootintegrations.mixin;

import com.lootintegrations.loot.INoMapContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExplorationMapFunction.class)
public class ExplorationFunctionMixin
{
    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private void onMapExlporation(final ItemStack p_80547_, final LootContext context, final CallbackInfoReturnable<ItemStack> cir)
    {
        if (context instanceof INoMapContext noMapContext && noMapContext.areMapsDisabled())
        {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}

package com.lootintegrations.mixin;

import com.lootintegrations.loot.LootModifierManager;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LootTable.class)
public class LootTableLootIntegrations
{
    @Inject(method = "getRandomItems(Lnet/minecraft/loot/LootContext;)Ljava/util/List;", at = @At("RETURN"))
    public void on(final LootContext context, final CallbackInfoReturnable<List<ItemStack>> cir)
    {
        LootModifierManager.applyTo(context, cir.getReturnValue());
    }
}

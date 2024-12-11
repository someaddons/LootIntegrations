package com.lootintegrations.mixin;

import com.lootintegrations.loot.ILootTableID;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootDataManager.class)
public class LootdataManagerMixin<T>
{
    @Inject(method = "getElement", at = @At("RETURN"))
    private void onGetElement(final LootDataId<T> lootDataId, final CallbackInfoReturnable<T> cir)
    {
        if (((Object) cir.getReturnValue()) instanceof ILootTableID lootTableID)
        {
            lootTableID.setId(lootDataId.location());
        }
    }
}

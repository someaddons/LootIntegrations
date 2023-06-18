package com.lootintegrations.mixin;

import com.google.gson.JsonElement;
import com.lootintegrations.LootintegrationsMod;
import com.lootintegrations.loot.ILootTableID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = LootDataType.class, priority = 10000)
public class LoottablesLoadMixin<T>
{
    @Inject(method = "deserialize", at = @At("RETURN"))
    private void onInit(
      final ResourceLocation resourceLocation, final JsonElement jsonElement, final CallbackInfoReturnable<Optional<T>> cir)
    {
        if (((Object) cir.getReturnValue()) instanceof ILootTableID)
        {
            ((ILootTableID) (Object) cir.getReturnValue()).setId(resourceLocation);
            LootintegrationsMod.LOGGER.warn("Loaded:" + resourceLocation);
        }
    }
}

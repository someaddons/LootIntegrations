package com.lootintegrations.mixin;

import com.google.gson.JsonElement;
import com.lootintegrations.LootintegrationsMod;
import com.lootintegrations.loot.ILootTableID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = LootTables.class, priority = 10000)
public class LoottablesLoadMixin
{
    @Shadow
    private Map<ResourceLocation, LootTable> tables;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("RETURN"))
    public void afterLoad(final Map<ResourceLocation, JsonElement> map, final ResourceManager resourceManager, final ProfilerFiller profilerFiller, final CallbackInfo ci)
    {
        for (final Map.Entry<ResourceLocation, LootTable> entry : tables.entrySet())
        {
            ((ILootTableID) entry.getValue()).setId(entry.getKey());
        }
        LootintegrationsMod.tables = tables;
    }
}

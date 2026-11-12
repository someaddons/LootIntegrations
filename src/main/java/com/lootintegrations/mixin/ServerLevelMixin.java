package com.lootintegrations.mixin;

import com.lootintegrations.loot.LootModifierManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class ServerLevelMixin
{
    @Inject(method = "findNearestMapStructure", at = @At("HEAD"), cancellable = true)
    private void preventMapSearch(TagKey<Structure> p_215012_, BlockPos p_215013_, int p_215014_, boolean p_215015_, CallbackInfoReturnable<BlockPos> cir)
    {
        if (LootModifierManager.disableMapSearch)
        {
            cir.setReturnValue(null);
        }
    }
}

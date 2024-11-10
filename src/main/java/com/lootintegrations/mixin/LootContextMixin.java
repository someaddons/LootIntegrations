package com.lootintegrations.mixin;

import com.lootintegrations.loot.INoMapContext;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LootContext.class)
public class LootContextMixin implements INoMapContext
{
    @Unique
    boolean disabledMaps = false;

    @Override
    public void disabledMaps()
    {
        disabledMaps = true;
    }

    @Override
    public boolean areMapsDisabled()
    {
        return disabledMaps;
    }
}

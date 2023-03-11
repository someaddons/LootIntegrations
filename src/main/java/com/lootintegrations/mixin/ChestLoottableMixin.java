package com.lootintegrations.mixin;

import com.lootintegrations.loot.IChestLoottable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RandomizableContainerBlockEntity.class)
public class ChestLoottableMixin implements IChestLoottable
{
    @Shadow
    @Nullable
    protected ResourceLocation lootTable;

    @Override
    public ResourceLocation getLoottable()
    {
        return lootTable;
    }
}

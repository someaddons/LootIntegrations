package com.lootintegrations.mixin;

import com.lootintegrations.loot.ILootTableID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LootTable.class)
public class LootTableMixin implements ILootTableID
{
    private ResourceLocation id = null;

    @Override
    public ResourceLocation getID()
    {
        return id;
    }

    @Override
    public void setId(final ResourceLocation id)
    {
        this.id = id;
    }
}
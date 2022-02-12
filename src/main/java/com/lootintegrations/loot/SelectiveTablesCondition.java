package com.lootintegrations.loot;

import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.ILootCondition;

import static net.minecraftforge.common.loot.LootTableIdCondition.LOOT_TABLE_ID;

public class SelectiveTablesCondition implements ILootCondition
{
    @Override
    public LootConditionType getType()
    {
        return LOOT_TABLE_ID;
    }

    @Override
    public boolean test(final LootContext lootContext)
    {
        return true;
    }
}

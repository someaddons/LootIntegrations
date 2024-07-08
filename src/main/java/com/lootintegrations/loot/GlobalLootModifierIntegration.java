package com.lootintegrations.loot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lootintegrations.LootintegrationsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lootintegrations.LootintegrationsMod.getLootTableId;

public class GlobalLootModifierIntegration
{
    private final ResourceLocation               location;
    public        ResourceLocation               lootTableId;
    public        Map<ResourceLocation, Integer> integratedTables = new HashMap<>();
    private       int                            fillSize         = 27;

    /**
     * Constructs a LootModifier.
     *
     * @param location
     */
    private GlobalLootModifierIntegration(final ResourceLocation location)
    {
        this.location = location;
    }

    /**
     * Applies the modifications to the loot list
     *
     * @param generatedLoot
     * @param context
     * @param lootTable
     */
    public void doApply(final List<ItemStack> generatedLoot, final LootContext context, final LootTable lootTable)
    {
        List<ItemStack> extraItems = new ArrayList<>();
        try
        {
            extraItems = context.getLevel().getServer().reloadableRegistries().get().registry(Registries.LOOT_TABLE).get().get(lootTableId).getRandomItems(context);
        }
        catch (Exception e)
        {
            LootintegrationsMod.LOGGER.debug("Loot generation of modifier:" + location + " for context failed for:" + lootTableId, e);
            return;
        }

        if (LootintegrationsMod.config.getCommonConfig().debugOutput)
        {
            LootintegrationsMod.LOGGER.info("Adding loot to: " + getLootTableId(lootTable, context.getLevel().getServer()) + "from: " + lootTableId + " caused by:" + location);
        }

        if (extraItems.isEmpty())
        {
            return;
        }

        int itemCount = integratedTables.getOrDefault(getLootTableId(lootTable, context.getLevel().getServer()), 1);
        extraItems = aggregateStacks(extraItems);

        if (!generatedLoot.isEmpty() && (generatedLoot.size() + itemCount) > fillSize)
        {
            List<ItemStack> newList = aggregateStacks(generatedLoot);
            generatedLoot.clear();
            generatedLoot.addAll(newList);
            if (generatedLoot.size() > fillSize)
            {
                int size = Math.min(generatedLoot.size(), (generatedLoot.size() + itemCount) - fillSize);
                for (int i = 0; i < size; i++)
                {
                    generatedLoot.remove(LootintegrationsMod.rand.nextInt(generatedLoot.size()));
                }
            }
        }

        for (int i = 0; i < itemCount; i++)
        {
            final ItemStack stack = extraItems.remove(LootintegrationsMod.rand.nextInt(extraItems.size()));
            generatedLoot.add(stack);
            if (LootintegrationsMod.config.getCommonConfig().debugOutput)
            {
                LootintegrationsMod.LOGGER.info("Adding loot to: " + getLootTableId(lootTable, context.getLevel().getServer()) + " item:" + stack.toString());
            }

            if (extraItems.isEmpty())
            {
                break;
            }
        }
    }

    /**
     * Aggregates the itemstacks in a list together, by item. May remove different variants of the same
     *
     * @param stacksIn
     * @return
     */
    private List<ItemStack> aggregateStacks(final List<ItemStack> stacksIn)
    {
        final Map<Item, ItemStack> aggregated = new HashMap<>();
        for (final ItemStack stack : stacksIn)
        {
            final ItemStack contained = aggregated.get(stack.getItem());
            if (contained == null)
            {
                aggregated.put(stack.getItem(), stack);
            }
            else
            {
                if (ItemStack.isSameItemSameComponents(stack, contained))
                {
                    contained.setCount(Math.min(contained.getCount() + stack.getCount(), Math.max(1, contained.getMaxStackSize() / 2)));
                }
            }
        }

        return new ArrayList<>(aggregated.values());
    }

    /**
     * Json ID names
     */
    private static final String LOOT_TABLE_ID          = "loot_table";
    private static final String INTEGRATED_LOOT_TABLES = "integrated_loot_tables";
    private static final String MAX_RESULT_ITEMCOUNT   = "max_result_itemcount";

    /**
     * Loads the loot modifiers from json
     */
    public static GlobalLootModifierIntegration read(final ResourceLocation location, final JsonElement data)
    {
        final GlobalLootModifierIntegration modifier = new GlobalLootModifierIntegration(location);

        JsonObject jsonData = (JsonObject) data;

        modifier.lootTableId = ResourceLocation.tryParse(jsonData.get(LOOT_TABLE_ID).getAsString());

        LootintegrationsMod.LOGGER.info("Parsing loot modifiers for:" + location + " with loottable: " + modifier.lootTableId);

        if (jsonData.has(MAX_RESULT_ITEMCOUNT))
        {
            modifier.fillSize = jsonData.get(MAX_RESULT_ITEMCOUNT).getAsInt();
        }

        final Map<ResourceLocation, Integer> integratedTables = new HashMap<>();
        for (final Map.Entry<String, JsonElement> element : jsonData.get(INTEGRATED_LOOT_TABLES).getAsJsonObject().entrySet())
        {
            final ResourceLocation integratedTable = ResourceLocation.tryParse(element.getKey());
            integratedTables.put(integratedTable, element.getValue().getAsInt());
        }

        modifier.integratedTables = integratedTables;

        return modifier;
    }

}

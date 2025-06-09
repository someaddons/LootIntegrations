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

import java.util.*;

import static com.lootintegrations.LootintegrationsMod.getLootTableId;

public class GlobalLootModifierIntegration
{
    private final ResourceLocation               location;
    public        ResourceLocation               lootTableId;
    public        Map<ResourceLocation, Integer> integratedTables = new HashMap<>();
    private       int                            fillSize         = 27;

    private static final Set<ResourceLocation> inbuiltTables = Set.of(LootintegrationsMod.resFor("chests/easy"),
        LootintegrationsMod.resFor("chests/medium"),
        LootintegrationsMod.resFor("chests/hard"),
        LootintegrationsMod.resFor("chests/nether"),
        LootintegrationsMod.resFor("chests/water"),
        LootintegrationsMod.resFor("chests/village"),
        LootintegrationsMod.resFor("chests/empty"));

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
            final LootContext generatingContext = new LootContext.Builder(context.params).create(Optional.empty());
            if (context instanceof INoMapContext noMapContext && LootintegrationsMod.config.getCommonConfig().skipMapItems)
            {
                noMapContext.disabledMaps();
            }

            extraItems = context.getLevel().getServer().reloadableRegistries().get().registry(Registries.LOOT_TABLE).get().get(lootTableId).getRandomItems(generatingContext);
        }
        catch (Exception e)
        {
            if (LootintegrationsMod.config.getCommonConfig().debugOutput)
            {
                LootintegrationsMod.LOGGER.warn("Loot generation of modifier:" + location + " for context failed for:" + lootTableId, e);
            }
            return;
        }

        if (LootintegrationsMod.config.getCommonConfig().debugOutput && !inbuiltTables.contains(lootTableId))
        {
            LootintegrationsMod.LOGGER.info("Adding loot to: " + getLootTableId(lootTable, context.getLevel().getServer()) + " from: " + lootTableId + " caused by:" + location);
        }

        if (extraItems.isEmpty())
        {
            if (LootintegrationsMod.config.getCommonConfig().debugOutput)
            {
                LootintegrationsMod.LOGGER.info("Could not generate items for loottable: " + lootTableId);
            }
            return;
        }

        int itemCount = integratedTables.getOrDefault(getLootTableId(lootTable, context.getLevel().getServer()), 1);
        extraItems = aggregateStacks(extraItems, false);

        if (extraItems.isEmpty())
        {
            return;
        }

        if (!generatedLoot.isEmpty() && (generatedLoot.size() + itemCount) > fillSize)
        {
            List<ItemStack> newList = aggregateStacks(generatedLoot, true);
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

        if (itemCount == 0)
        {
            return;
        }

        int[] weights = new int[extraItems.size()];
        int totalWeight = 0;

        int size = extraItems.size();
        for (int i = 0; i < size; i++)
        {
            int weight = calcWeightForStack(extraItems.get(i));
            totalWeight += weight;
            weights[i] = totalWeight;
        }

        for (int i = 0; i < itemCount; i++)
        {
            if (totalWeight <= 0)
            {
                return;
            }

            int weight = LootintegrationsMod.rand.nextInt(totalWeight);
            int index = -1;
            int removedWeight = 0;
            ItemStack stack = null;
            for (int j = 0; j < size; j++)
            {
                if (index == -1 && weight < weights[j])
                {
                    index = j;
                    weights[j] = 0;
                    stack = extraItems.get(index);
                    removedWeight = calcWeightForStack(stack);
                    totalWeight -= removedWeight;
                    continue;
                }

                if (index != -1)
                {
                    weights[j] -= removedWeight;
                }
            }

            if (stack == null)
            {
                continue;
            }

            boolean sameItem = false;

            if (LootintegrationsMod.config.getCommonConfig().skipExistingItems)
            {
                for (int j = 0; j < generatedLoot.size(); j++)
                {
                    if (ItemStack.isSameItemSameComponents(generatedLoot.get(j), stack))
                    {
                        sameItem = true;
                        break;
                    }
                }
            }

            if (!sameItem)
            {
                generatedLoot.add(stack);
                if (LootintegrationsMod.config.getCommonConfig().debugOutput)
                {
                    LootintegrationsMod.LOGGER.info("Adding loot to: " + getLootTableId(lootTable, context.getLevel().getServer()) + " item:" + stack.toString());
                }
            }
            else
            {
                i--;
            }

            if (extraItems.isEmpty())
            {
                break;
            }
        }
    }

    /**
     * Determines the weight for a stack
     * @param stack
     * @return
     */
    private int calcWeightForStack(final ItemStack stack)
    {
        return stack.getItemHolder().unwrapKey().get().location().getNamespace().equals("minecraft") ? 1 : LootintegrationsMod.config.getCommonConfig().moddedItemWeight + 1;
    }

    /**
     * Aggregates the itemstacks in a list together, by item
     *
     * @param stacksIn
     * @param originalLoot
     * @return
     */
    private List<ItemStack> aggregateStacks(final List<ItemStack> stacksIn, final boolean originalLoot)
    {
        final Map<Item, ItemStack> aggregated = new HashMap<>();
        for (final ItemStack stack : stacksIn)
        {
            final ItemStack contained = aggregated.get(stack.getItem());

            if (stack.isEmpty() || (!originalLoot && stack.is(LootModifierManager.IGNORED_FOR_LOOT)))
            {
                continue;
            }

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

        if (LootintegrationsMod.config.getCommonConfig().debugOutput && location.getPath().contains("lootintegrations_"))
        {
            LootintegrationsMod.LOGGER.info("Parsing loot modifiers for:" + location + " with loottable: " + modifier.lootTableId);
        }

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

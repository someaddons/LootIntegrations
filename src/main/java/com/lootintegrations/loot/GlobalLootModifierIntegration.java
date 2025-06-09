package com.lootintegrations.loot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lootintegrations.LootintegrationsMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

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
     */
    @Nonnull
    public void doApply(final List<ItemStack> generatedLoot, final LootContext context)
    {
        List<ItemStack> extraItems;
        try
        {
            if (context instanceof INoMapContext noMapContext && LootintegrationsMod.config.getCommonConfig().skipMapItems)
            {
                noMapContext.disabledMaps();
            }

            extraItems = ServerLifecycleHooks.getCurrentServer().getLootTables().get(lootTableId).getRandomItems(context);
        }
        catch (Exception e)
        {
            LootintegrationsMod.LOGGER.debug("Loot generation of modifier:" + location + " for context failed for:" + lootTableId, e);
            return;
        }

        if (LootintegrationsMod.config.getCommonConfig().debugOutput && !inbuiltTables.contains(lootTableId))
        {
            LootintegrationsMod.LOGGER.info("Adding loot to: " + context.getQueriedLootTableId() + "from: " + lootTableId + " caused by:" + location);
        }

        if (extraItems.isEmpty())
        {
            return;
        }

        int itemCount = integratedTables.getOrDefault(context.getQueriedLootTableId(), 1);
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
                    if (ItemStack.isSameItemSameTags(generatedLoot.get(j), stack))
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
                    LootintegrationsMod.LOGGER.info("Adding loot to: " + (context.getQueriedLootTableId() + " item:" + stack.toString()));
                }
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

            if (stack.isEmpty())
            {
                continue;
            }

            if (contained == null)
            {
                aggregated.put(stack.getItem(), stack);
            }
            else
            {
                if (compareItemStacksIgnoreStackSize(stack, contained, false, true))
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

        modifier.lootTableId = new ResourceLocation(jsonData.get(LOOT_TABLE_ID).getAsString());

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
            final ResourceLocation integratedTable = new ResourceLocation(element.getKey());
            integratedTables.put(integratedTable, element.getValue().getAsInt());
        }

        modifier.integratedTables = integratedTables;

        return modifier;
    }

    /**
     * Compares two stacks ignoring count
     *
     * @param itemStack1
     * @param itemStack2
     * @param matchDamage
     * @param matchNBT
     * @return
     */
    public static boolean compareItemStacksIgnoreStackSize(
      final ItemStack itemStack1,
      final ItemStack itemStack2,
      final boolean matchDamage,
      final boolean matchNBT)
    {
        if (itemStack1.isEmpty() && itemStack2.isEmpty())
        {
            return true;
        }

        if (itemStack1.isEmpty() && !itemStack2.isEmpty()
              || !itemStack1.isEmpty() && itemStack2.isEmpty())
        {
            return false;
        }

        if (itemStack1 == itemStack2)
        {
            return true;
        }

        if (!matchDamage || itemStack1.getDamageValue() == itemStack2.getDamageValue())
        {
            if (!matchNBT)
            {
                return true;
            }

            if (itemStack1.hasTag() && itemStack2.hasTag())
            {
                CompoundTag nbt1 = itemStack1.getTag();
                CompoundTag nbt2 = itemStack2.getTag();

                for (String key : nbt1.getAllKeys())
                {
                    if (!matchDamage && key.equals("Damage"))
                    {
                        continue;
                    }
                    if (!nbt2.contains(key) || !nbt1.get(key).equals(nbt2.get(key)))
                    {
                        return false;
                    }
                }

                return nbt1.getAllKeys().size() == nbt2.getAllKeys().size();
            }
            else
            {
                return (!itemStack1.hasTag() || itemStack1.getTag().isEmpty())
                         && (!itemStack2.hasTag() || itemStack2.getTag().isEmpty());
            }
        }
        return false;
    }
}

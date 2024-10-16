package com.lootintegrations.loot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lootintegrations.LootintegrationsMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     */
    @Nonnull
    public void doApply(final List<ItemStack> generatedLoot, final LootContext context)
    {
        List<ItemStack> extraItems = ServerLifecycleHooks.getCurrentServer().getLootTables().get(lootTableId).getRandomItems(context);

        int itemCount = integratedTables.getOrDefault(context.getQueriedLootTableId(), 1);
        extraItems = aggregateStacks(extraItems);

        if ((generatedLoot.size() + itemCount) > fillSize)
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

        if (!extraItems.isEmpty())
        {
            for (int i = 0; i < itemCount; i++)
            {
                final ItemStack stack = extraItems.remove(LootintegrationsMod.rand.nextInt(extraItems.size()));
                generatedLoot.add(stack);
                if (LootintegrationsMod.config.getCommonConfig().debugOutput.get())
                {
                    LootintegrationsMod.LOGGER.info("Adding loot to:" + context.getQueriedLootTableId() + " item:" + stack.toString());
                }

                if (extraItems.isEmpty())
                {
                    break;
                }
            }
        }
    }

    /**
     * Aggregates the itemstacks in a list together, by item. May remove different variants of the same
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
                CompoundNBT nbt1 = itemStack1.getTag();
                CompoundNBT nbt2 = itemStack2.getTag();

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

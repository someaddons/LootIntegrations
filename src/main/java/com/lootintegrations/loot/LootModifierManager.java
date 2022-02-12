package com.lootintegrations.loot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.lootintegrations.LootintegrationsMod;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootModifierManager extends JsonReloadListener
{
    public static final  Map<ResourceLocation, List<GlobalLootModifierIntegration>> lootOptionsMap = new HashMap<>();
    private static final Gson                                                       GSON           = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public LootModifierManager()
    {
        super(GSON, "loot");
    }

    private static boolean applying = false;

    /**
     * On loot fill we apply modifiers
     *
     * @param context
     * @param items
     * @return
     */
    public static void applyTo(final LootContext context, final List<ItemStack> items)
    {
        if (applying)
        {
            return;
        }

        applying = true;

        // apply modifiers
        List<GlobalLootModifierIntegration> modifiers = lootOptionsMap.get(context.getQueriedLootTableId());
        if (modifiers != null && !modifiers.isEmpty())
        {
            for (final GlobalLootModifierIntegration modifier : modifiers)
            {
                modifier.doApply(items, context);
            }
        }

        applying = false;
    }

    @Override
    protected void apply(
      final Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, final IResourceManager iResourceManager, final IProfiler iProfiler)
    {
        lootOptionsMap.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceLocationJsonElementMap.entrySet())
        {
            if (!entry.getKey().getNamespace().equals(LootintegrationsMod.MODID))
            {
                continue;
            }

            try
            {
                final GlobalLootModifierIntegration modifier = GlobalLootModifierIntegration.read(entry.getKey(), entry.getValue());
                for (final ResourceLocation integratedTable : modifier.integratedTables.keySet())
                {
                    lootOptionsMap.computeIfAbsent(integratedTable, e -> new ArrayList<>()).add(modifier);
                }
            }
            catch (Error e)
            {
                LootintegrationsMod.LOGGER.warn("Failed to load loot modifier file:" + entry.getKey(), e);
            }
        }
    }
}

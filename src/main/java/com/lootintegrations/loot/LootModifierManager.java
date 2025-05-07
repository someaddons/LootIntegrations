package com.lootintegrations.loot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.lootintegrations.LootintegrationsMod;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.*;

public class LootModifierManager extends SimpleJsonResourceReloadListener<JsonElement> implements IdentifiableResourceReloadListener
{
    public static final  Map<ResourceLocation, List<GlobalLootModifierIntegration>> lootOptionsMap = new HashMap<>();
    private static final Gson         GSON             = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final  TagKey<Item> IGNORED_FOR_LOOT = TagKey.create(Registries.ITEM, (ResourceLocation.tryParse("lootintegrations:ignored")));

    public LootModifierManager()
    {
        super(ExtraCodecs.JSON, FileToIdConverter.json("loot"));
    }

    private static Set<ResourceLocation> applying = new HashSet<>();

    /**
     * On loot fill we apply modifiers
     *
     * @param context
     * @param items
     * @return
     */
    public static void applyTo(final LootContext context, final List<ItemStack> items, final LootTable lootTable)
    {
        if (lootTable == null)
        {
            return;
        }

        final ResourceLocation lootTableID = LootintegrationsMod.getLootTableId(lootTable, context.getLevel().getServer());
        if (lootTableID == null)
        {
            return;
        }

        if (applying.contains(lootTableID))
        {
            return;
        }

        // apply modifiers
        List<GlobalLootModifierIntegration> modifiers = lootOptionsMap.get(lootTableID);
        if (modifiers != null && !modifiers.isEmpty())
        {
            applying.add(lootTableID);
            for (final GlobalLootModifierIntegration modifier : modifiers)
            {
                modifier.doApply(items, context, lootTable);
            }
            applying.remove(lootTableID);
        }
    }

    @Override
    protected void apply(
      final Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, final ResourceManager iResourceManager, final ProfilerFiller iProfiler)
    {
        lootOptionsMap.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceLocationJsonElementMap.entrySet())
        {
            if (!entry.getKey().getNamespace().equals(LootintegrationsMod.MODID))
            {
                LootintegrationsMod.LOGGER.warn("Ignoring loot modifiers for:" + entry.getKey() + " use this folder name:" + LootintegrationsMod.MODID);
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

    @Override
    public ResourceLocation getFabricId()
    {
        return ResourceLocation.tryBuild(LootintegrationsMod.MODID, "lootintegrationreloadlistener");
    }
}

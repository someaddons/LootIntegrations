package com.lootintegrations;

import com.cupboard.config.CupboardConfig;
import com.lootintegrations.config.CommonConfiguration;
import com.lootintegrations.loot.LootModifierManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.loot.LootTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

import static net.minecraft.server.packs.PackType.SERVER_DATA;

// The value here should match an entry in the META-INF/mods.toml file
public class LootintegrationsMod implements ModInitializer
{
    public static final String                              MODID  = "lootintegrations";
    public static final Logger                              LOGGER = LogManager.getLogger();
    public static       Random                              rand   = new Random();
    public static       CupboardConfig<CommonConfiguration> config = new CupboardConfig<>(MODID, new CommonConfiguration());

    public LootintegrationsMod()
    {

    }

    @Override
    public void onInitialize()
    {
        ResourceManagerHelper.get(SERVER_DATA).registerReloadListener(new LootModifierManager());
    }

    public static ResourceLocation getLootTableId(final LootTable table, final MinecraftServer server)
    {
        return server.reloadableRegistries().get().registry(Registries.LOOT_TABLE).get().getKey(table);
    }
}

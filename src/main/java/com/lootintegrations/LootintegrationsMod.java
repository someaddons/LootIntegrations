package com.lootintegrations;

import com.cupboard.config.CupboardConfig;
import com.lootintegrations.config.CommonConfiguration;
import com.lootintegrations.event.EventHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

import static com.lootintegrations.LootintegrationsMod.MODID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MODID)
public class LootintegrationsMod
{
    public static final String                              MODID  = "lootintegrations";
    public static final Logger                              LOGGER = LogManager.getLogger();
    public static       CupboardConfig<CommonConfiguration> config = new CupboardConfig<>(MODID, new CommonConfiguration());
    public static       Random                              rand   = new Random();

    public LootintegrationsMod(IEventBus modEventBus, ModContainer modContainer)
    {
        NeoForge.EVENT_BUS.register(EventHandler.class);
        modEventBus.addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        LOGGER.info(MODID + " mod initialized");
    }

    public static ResourceLocation getLootTableId(final LootTable table, final MinecraftServer server)
    {
        return server.reloadableRegistries().get().registry(Registries.LOOT_TABLE).get().getKey(table);
    }
}

package com.lootintegrations.event;

import com.cupboard.util.ResourceLocation;
import com.lootintegrations.LootintegrationsMod;
import com.lootintegrations.loot.LootModifierManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Forge event bus handler, ingame events are fired here
 */
public class EventHandler
{
    @SubscribeEvent
    public static void onAddReloadListenerEvent(final AddServerReloadListenersEvent event)
    {
        event.addListener(ResourceLocation.fromNamespaceAndPath(LootintegrationsMod.MODID,"reloadlistener"),new LootModifierManager());
    }

    @SubscribeEvent
    public static void playerClickBlockEvent(final PlayerInteractEvent.RightClickBlock event)
    {
        if (!LootintegrationsMod.config.getCommonConfig().showcontainerloottable || event.getLevel().isClientSide())
        {
            return;
        }

        final BlockEntity te = event.getEntity().level().getBlockEntity(event.getPos());
        if (te instanceof RandomizableContainerBlockEntity && ((RandomizableContainerBlockEntity) te).getLootTable() != null)
        {
            ((ServerPlayer)event.getEntity())
                .sendSystemMessage(Component.literal("[Loottable: " + ((RandomizableContainerBlockEntity) te).getLootTable().identifier() + "]")
                                   .setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)
                                       .withClickEvent(new ClickEvent.CopyToClipboard(((RandomizableContainerBlockEntity) te).getLootTable().identifier().toString()))));
        }
    }
}

package com.lootintegrations.event;

import com.lootintegrations.LootintegrationsMod;
import com.lootintegrations.loot.LootModifierManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Forge event bus handler, ingame events are fired here
 */
public class EventHandler
{
    @SubscribeEvent
    public static void onAddReloadListenerEvent(final AddReloadListenerEvent event)
    {
        event.addListener(new LootModifierManager());
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
            event.getEntity()
              .sendSystemMessage(Component.literal("[Loottable: " + ((RandomizableContainerBlockEntity) te).getLootTable().location() + "]")
                                   .setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)
                                               .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,
                                                 ((RandomizableContainerBlockEntity) te).getLootTable().location().toString()))));
        }
    }
}

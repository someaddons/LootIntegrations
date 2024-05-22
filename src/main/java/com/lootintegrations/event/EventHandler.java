package com.lootintegrations.event;

import com.lootintegrations.LootintegrationsMod;
import com.lootintegrations.loot.LootModifierManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        if (!LootintegrationsMod.config.getCommonConfig().showcontainerloottable || event.getEntity().level.isClientSide())
        {
            return;
        }

        final BlockEntity te = event.getEntity().level.getBlockEntity(event.getPos());
        if (te instanceof RandomizableContainerBlockEntity && ((RandomizableContainerBlockEntity) te).lootTable != null)
        {
            event.getEntity()
              .sendMessage(new TextComponent("[Loottable: " + ((RandomizableContainerBlockEntity) te).lootTable + "]").setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,
                  ((RandomizableContainerBlockEntity) te).lootTable.toString()))), event.getEntity().getUUID());
        }
    }
}

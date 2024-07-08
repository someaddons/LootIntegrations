package com.lootintegrations.mixin;

import com.lootintegrations.LootintegrationsMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class PlayerInteractMixin
{
    @Inject(method = "useItemOn", at = @At("HEAD"))
    private void playerClickBlockEvent(
      final ServerPlayer serverPlayer,
      final Level level,
      final ItemStack itemStack,
      final InteractionHand interactionHand,
      final BlockHitResult blockHitResult, final CallbackInfoReturnable<InteractionResult> cir)
    {
        if (LootintegrationsMod.config.getCommonConfig().showcontainerloottable)
        {
            final BlockEntity te = level.getBlockEntity(blockHitResult.getBlockPos());
            if (te instanceof RandomizableContainerBlockEntity && ((RandomizableContainerBlockEntity) te).getLootTable() != null)
            {
                serverPlayer
                  .sendSystemMessage(Component.literal("[Loottable: " + ((RandomizableContainerBlockEntity) te).getLootTable().location() + "]")
                                       .setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,
                      ((RandomizableContainerBlockEntity) te).getLootTable().location().toString()))));
            }
        }
    }
}

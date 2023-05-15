package fuzs.easyshulkerboxes.mixin;

import fuzs.easyshulkerboxes.api.world.item.container.ItemContainerProvider;
import fuzs.easyshulkerboxes.world.item.container.helper.ContainerItemHelper;
import fuzs.easyshulkerboxes.world.item.storage.ItemContainerProvidersListener;
import fuzs.puzzleslib.proxy.Proxy;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {

    @Inject(method = "overrideStackedOnOther", at = @At("HEAD"), cancellable = true)
    public void overrideStackedOnOther(Slot slot, ClickAction clickAction, Player player, CallbackInfoReturnable<Boolean> callback) {
        ItemStack containerStack = ItemStack.class.cast(this);
        ItemContainerProvider provider = ItemContainerProvidersListener.INSTANCE.get(containerStack.getItem());
        if (provider != null && provider.allowsPlayerInteractions(containerStack, player)) {
            boolean result = ContainerItemHelper.overrideStackedOnOther(() -> provider.getItemContainer(containerStack, player, true), slot, clickAction, player, stack -> provider.getAcceptableItemCount(containerStack, stack, player), SoundEvents.BUNDLE_INSERT, SoundEvents.BUNDLE_REMOVE_ONE);
            if (result) provider.broadcastContainerChanges(player);
            callback.setReturnValue(result);
        }
    }

    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    public void overrideOtherStackedOnMe(ItemStack stackOnMe, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess, CallbackInfoReturnable<Boolean> callback) {
        ItemStack containerStack = ItemStack.class.cast(this);
        ItemContainerProvider provider = ItemContainerProvidersListener.INSTANCE.get(containerStack.getItem());
        if (provider != null && provider.allowsPlayerInteractions(containerStack, player)) {
            boolean result = ContainerItemHelper.overrideOtherStackedOnMe(() -> provider.getItemContainer(containerStack, player, true), stackOnMe, slot, clickAction, player, slotAccess, stack -> provider.getAcceptableItemCount(containerStack, stack, player), SoundEvents.BUNDLE_INSERT, SoundEvents.BUNDLE_REMOVE_ONE);
            if (result) provider.broadcastContainerChanges(player);
            callback.setReturnValue(result);
        }
    }

    @Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)
    public void getTooltipImage(CallbackInfoReturnable<Optional<TooltipComponent>> callback) {
        ItemStack containerStack = ItemStack.class.cast(this);
        ItemContainerProvider provider = ItemContainerProvidersListener.INSTANCE.get(containerStack.getItem());
        if (provider != null && provider.canProvideTooltipImage(containerStack, Proxy.INSTANCE.getClientPlayer())) {
            callback.setReturnValue(provider.getTooltipImage(containerStack, Proxy.INSTANCE.getClientPlayer()));
        }
    }
}

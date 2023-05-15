package fuzs.easyshulkerboxes.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.easyshulkerboxes.EasyShulkerBoxes;
import fuzs.easyshulkerboxes.api.world.item.container.ItemContainerProvider;
import fuzs.easyshulkerboxes.config.ClientConfig;
import fuzs.easyshulkerboxes.config.ServerConfig;
import fuzs.easyshulkerboxes.mixin.client.accessor.ScreenAccessor;
import fuzs.easyshulkerboxes.network.client.C2SContainerClientInputMessage;
import fuzs.easyshulkerboxes.world.inventory.helper.ContainerSlotHelper;
import fuzs.easyshulkerboxes.world.item.storage.ItemContainerProvidersListener;
import fuzs.puzzleslib.client.gui.screens.CommonScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class ClientInputActionHandler {
    private static int lastSentContainerSlot = -1;
    private static boolean lastSentExtractSingleItem;

    public static Optional<Unit> onBeforeKeyPressed(Screen screen, int keyCode, int scanCode, int modifiers) {
        // this must be sent before any slot click action is performed server side, by vanilla this can be caused by either mouse clicks (normal menu interactions)
        // or key presses (hotbar keys for swapping items to those slots)
        // this is already added via mixin to where vanilla sends the click packet, but creative screen doesn't use it and you never know with other mods...
        ensureHasSentContainerClientInput();
        return Optional.empty();
    }

    public static Optional<Unit> onBeforeMousePressed(Screen screen, double mouseX, double mouseY, int button) {
        // this must be sent before any slot click action is performed server side, by vanilla this can be caused by either mouse clicks (normal menu interactions)
        // or key presses (hotbar keys for swapping items to those slots)
        // this is already added via mixin to where vanilla sends the click packet, but creative screen doesn't use it and you never know with other mods...
        ensureHasSentContainerClientInput();
        return Optional.empty();
    }

    public static void onAfterRender(Screen screen, PoseStack matrices, int mouseX, int mouseY, float tickDelta) {
        // renders vanilla item tooltips when a stack is carried and the cursor hovers over a container item
        // intended to be used with single item extraction/insertion feature to be able to continuously see what's going on in the container item
        if (!shouldHandleMouseScroll(screen)) return;
        if (!EasyShulkerBoxes.CONFIG.get(ClientConfig.class).extractSingleItem.isActive()) return;
        AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) screen;
        if (!containerScreen.getMenu().getCarried().isEmpty()) {
            Slot slot = CommonScreens.INSTANCE.getHoveredSlot(containerScreen);
            if (slot != null) {
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty() && ItemContainerProvidersListener.INSTANCE.get(stack.getItem()) != null) {
                    ((ScreenAccessor) screen).easyshulkerboxes$callRenderTooltip(matrices, stack, mouseX, mouseY);
                }
            }
        }
    }

    public static Optional<Unit> onBeforeMouseScroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // allows to scroll between filled slots on a container items tooltip to select the slot to be interacted with next
        if (!shouldHandleMouseScroll(screen)) return Optional.empty();
        if (!EasyShulkerBoxes.CONFIG.get(ServerConfig.class).allowSlotCycling) return Optional.empty();
        Slot hoveredSlot = CommonScreens.INSTANCE.getHoveredSlot((AbstractContainerScreen<?>) screen);
        if (hoveredSlot != null) {
            ItemStack stack = hoveredSlot.getItem();
            ItemContainerProvider provider = ItemContainerProvidersListener.INSTANCE.get(stack.getItem());
            if (provider != null && provider.hasItemContainerData(stack)) {
                int signum = (int) Math.signum(verticalAmount);
                if (signum != 0) {
                    Player player = CommonScreens.INSTANCE.getMinecraft(screen).player;
                    int currentContainerSlot = ContainerSlotHelper.getCurrentContainerSlot(player);
                    currentContainerSlot = ContainerSlotHelper.findClosestSlotWithContent(provider.getItemContainer(stack, player, false), currentContainerSlot, signum < 0);
                    ContainerSlotHelper.setCurrentContainerSlot(player, currentContainerSlot);
                }
                return Optional.of(Unit.INSTANCE);
            }
        }
        return Optional.empty();
    }

    private static boolean shouldHandleMouseScroll(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?>)) return false;
        return EasyShulkerBoxes.CONFIG.get(ClientConfig.class).revealContents.isActive();
    }

    public static void ensureHasSentContainerClientInput() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof AbstractContainerScreen<?>)) return;
        int currentContainerSlot = ContainerSlotHelper.getCurrentContainerSlot(minecraft.player);
        boolean extractSingleItem = EasyShulkerBoxes.CONFIG.get(ClientConfig.class).extractSingleItem.isActive();
        if (currentContainerSlot != lastSentContainerSlot || extractSingleItem != lastSentExtractSingleItem) {
            lastSentContainerSlot = currentContainerSlot;
            lastSentExtractSingleItem = extractSingleItem;
            // this is where the client sets this value, so it's important to call before click actions even when syncing isn't so important (applies mostly to creative menu)
            ContainerSlotHelper.extractSingleItem(minecraft.player, extractSingleItem);
            EasyShulkerBoxes.NETWORK.sendToServer(new C2SContainerClientInputMessage(currentContainerSlot, extractSingleItem));
        }
    }
}

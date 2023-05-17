package fuzs.easyshulkerboxes.api.container.v1;

import com.google.gson.JsonObject;
import fuzs.easyshulkerboxes.mixin.accessor.BundleItemAccessor;
import fuzs.easyshulkerboxes.world.inventory.tooltip.ModBundleTooltip;
import fuzs.easyshulkerboxes.impl.world.item.container.ContainerItemHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BundleProvider extends NestedTagItemProvider {
    private final int capacity;

    public BundleProvider(int capacity) {
        this(capacity, DyeColor.BROWN, ContainerItemHelper.TAG_ITEMS);
    }

    public BundleProvider(int capacity, @Nullable DyeColor dyeColor, String... nbtKey) {
        super(dyeColor, nbtKey);
        this.capacity = capacity;
    }

    @Override
    public SimpleContainer getItemContainer(ItemStack containerStack, Player player, boolean allowSaving) {
        // add one additional slot, so we can add items in the inventory
        return ContainerItemHelper.loadItemContainer(containerStack, this, items -> new SimpleContainer(items + 1), allowSaving, this.getNbtKey());
    }

    @Override
    public boolean isItemAllowedInContainer(ItemStack containerStack, ItemStack stackToAdd) {
        return super.isItemAllowedInContainer(containerStack, stackToAdd) && stackToAdd.getItem().canFitInsideContainerItems();
    }

    @Override
    public boolean canAddItem(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        return ContainerItemHelper.getAvailableBundleItemSpace(containerStack, stackToAdd, this.capacity) > 0;
    }

    @Override
    public int getAcceptableItemCount(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        return Math.min(ContainerItemHelper.getAvailableBundleItemSpace(containerStack, stackToAdd, this.capacity), super.getAcceptableItemCount(containerStack, stackToAdd, player));
    }

    @Override
    public boolean canProvideTooltipImage(ItemStack containerStack, Player player) {
        return true;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack containerStack, Player player) {
        // make sure to always override bundle tooltip, as otherwise vanilla tooltip would show for empty bundles
        if (!this.hasItemContainerData(containerStack)) return Optional.empty();
        return super.getTooltipImage(containerStack, player);
    }

    @Override
    public TooltipComponent createTooltipImageComponent(ItemStack containerStack, NonNullList<ItemStack> items) {
        return new ModBundleTooltip(items, BundleItemAccessor.easyshulkerboxes$getContentWeight(containerStack) >= this.capacity, this.getBackgroundColor());
    }

    @Override
    public void toJson(JsonObject jsonObject) {
        jsonObject.addProperty("capacity", this.capacity);
        super.toJson(jsonObject);
    }
}
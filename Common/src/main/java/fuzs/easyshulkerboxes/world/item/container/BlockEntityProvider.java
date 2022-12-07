package fuzs.easyshulkerboxes.world.item.container;

import com.google.gson.JsonObject;
import fuzs.easyshulkerboxes.world.item.container.helper.ContainerItemHelper;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public class BlockEntityProvider extends SimpleItemProvider {
    private final ResourceLocation blockEntityTypeId;
    @Nullable
    private BlockEntityType<?> blockEntityType;

    public BlockEntityProvider(BlockEntityType<?> blockEntityType, int inventoryWidth, int inventoryHeight) {
        this(Registry.BLOCK_ENTITY_TYPE.getKey(blockEntityType), inventoryWidth, inventoryHeight);
    }

    public BlockEntityProvider(ResourceLocation blockEntityTypeId, int inventoryWidth, int inventoryHeight) {
        super(inventoryWidth, inventoryHeight);
        this.blockEntityTypeId = blockEntityTypeId;
    }

    public BlockEntityProvider(BlockEntityType<?> blockEntityType, int inventoryWidth, int inventoryHeight, @Nullable DyeColor dyeColor, String... nbtKey) {
        this(Registry.BLOCK_ENTITY_TYPE.getKey(blockEntityType), inventoryWidth, inventoryHeight, dyeColor, nbtKey);
    }

    public BlockEntityProvider(ResourceLocation blockEntityTypeId, int inventoryWidth, int inventoryHeight, @Nullable DyeColor dyeColor, String... nbtKey) {
        super(inventoryWidth, inventoryHeight, dyeColor, nbtKey);
        this.blockEntityTypeId = blockEntityTypeId;
    }

    @Override
    public boolean canProvideContainer(ItemStack containerStack, Player player) {
        return super.canProvideContainer(containerStack, player) && player.getAbilities().instabuild;
    }

    @Override
    public SimpleContainer getItemContainer(ItemStack containerStack, Player player, boolean allowSaving) {
        return ContainerItemHelper.loadItemContainer(containerStack, this, this.getInventorySize(), allowSaving, this.getNbtKey());
    }

    @Override
    protected @Nullable CompoundTag getItemDataBase(ItemStack containerStack) {
        return BlockItem.getBlockEntityData(containerStack);
    }

    @Override
    protected void setItemDataToStack(ItemStack containerStack, @Nullable CompoundTag tag) {
        BlockItem.setBlockEntityData(containerStack, this.getBlockEntityType(), tag == null ? new CompoundTag() : tag);
    }

    public BlockEntityType<?> getBlockEntityType() {
        if (this.blockEntityType == null) {
            if (Registry.BLOCK_ENTITY_TYPE.containsKey(this.blockEntityTypeId)) {
                this.blockEntityType = Registry.BLOCK_ENTITY_TYPE.get(this.blockEntityTypeId);
            } else {
                throw new IllegalArgumentException("%s is not a valid block entity type".formatted(this.blockEntityTypeId));
            }
        }
        return this.blockEntityType;
    }

    @Override
    public void toJson(JsonObject jsonObject) {
        super.toJson(jsonObject);
        jsonObject.addProperty("block_entity_type", this.blockEntityTypeId.toString());
    }
}

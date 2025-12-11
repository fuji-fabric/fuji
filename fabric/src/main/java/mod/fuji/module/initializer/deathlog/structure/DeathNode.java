package mod.fuji.module.initializer.deathlog.structure;

import mod.fuji.core.auxiliary.ChronosUtil;
import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.InventoryHelper;
import mod.fuji.core.auxiliary.minecraft.NbtHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.ItemStackHelper;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.module.initializer.deathlog.DeathLogInitializer;
import java.io.IOException;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class DeathNode {

    /* Schema keys. */
    public static final String DEATHS_KEY = "Deaths";

    private static final String REMARK_KEY = "remark";
    private static final String TIME_KEY = "time";
    private static final String REASON_KEY = "reason";
    private static final String DIMENSION_KEY = "dimension";
    private static final String X_KEY = "x";
    private static final String Y_KEY = "y";
    private static final String Z_KEY = "z";

    private static final String ARMOR_KEY = "armor";
    private static final String OFFHAND_KEY = "offhand";
    private static final String ITEM_KEY = "item";
    private static final String SCORE_KEY = "score";
    private static final String XP_LEVEL_KEY = "xp_level";
    private static final String XP_PROGRESS_KEY = "xp_progress";
    private static final String INVENTORY_KEY = "inventory";

    /* Death node props. */
    public String time;
    public String dimension;
    public double x;
    public double y;
    public double z;
    public String reason;

    public List<ItemStack> main;
    public List<ItemStack> armor;
    public List<ItemStack> offhand;
    public int score;
    public int expLevel;
    public float expProgress;

    public static DeathNode fromNbt(CompoundTag nbt) {
        DeathNode deathNode = new DeathNode();

        /* Read remark tag. */
        CompoundTag remarkTag = NbtHelper.Primitives.getCompound(nbt, REMARK_KEY).get();

        deathNode.time = NbtHelper.Primitives.getString(remarkTag, TIME_KEY).get();
        deathNode.dimension = NbtHelper.Primitives.getString(remarkTag, DIMENSION_KEY).get();
        deathNode.x = NbtHelper.Primitives.getDouble(remarkTag, X_KEY).get();
        deathNode.y = NbtHelper.Primitives.getDouble(remarkTag, Y_KEY).get();
        deathNode.z = NbtHelper.Primitives.getDouble(remarkTag, Z_KEY).get();
        deathNode.reason = NbtHelper.Primitives.getString(remarkTag, REASON_KEY).get();

        /* Read inventory tag. */
        CompoundTag inventoryNode = NbtHelper.Primitives.getCompound(nbt, INVENTORY_KEY).get();

        // restore main stacks (1*9 slots + 3*9 slots)
        deathNode.main = ItemStackHelper.Codec.readSlotsNode((ListTag) inventoryNode.get(ITEM_KEY));

        deathNode.armor = ItemStackHelper.Codec.readSlotsNode((ListTag) inventoryNode.get(ARMOR_KEY));

        deathNode.offhand = ItemStackHelper.Codec.readSlotsNode((ListTag) inventoryNode.get(OFFHAND_KEY));

        deathNode.score = NbtHelper.Primitives.getInt(inventoryNode, SCORE_KEY).get();
        deathNode.expLevel = NbtHelper.Primitives.getInt(inventoryNode, XP_LEVEL_KEY).get();
        deathNode.expProgress = NbtHelper.Primitives.getFloat(inventoryNode, XP_PROGRESS_KEY).get();

        return deathNode;
    }

    @SneakyThrows(IOException.class)
    public static void createDeathNode(@NotNull ServerPlayer player) {
        if (player.getInventory().isEmpty()) return;

        NbtHelper.Storage.withNbtFile(DeathLogInitializer.getDeathDataPath(PlayerHelper.getPlayerName(player)), root -> {
            ListTag deathNodeList = NbtHelper.Walker.getOrCreateNbtElement(root, DEATHS_KEY, new ListTag());
            deathNodeList.add(makeDeathNodeNbt(player));
        });
    }

    private static void writeRemarkNode(@NotNull CompoundTag parent, @NotNull ServerPlayer player) {
        String time = ChronosUtil.Formatter.getFormattedCurrentDate();
        String reason = player.getCombatTracker().getDeathMessage().getString();
        String dimension = RegistryHelper
            .getIdentifier(PlayerHelper.getServerWorld(player).dimension())
            .toString();
        Vec3 position = EntityHelper.getPos(player);

        CompoundTag remarkTag = new CompoundTag();
        remarkTag.putString(TIME_KEY, time);
        remarkTag.putString(REASON_KEY, reason);
        remarkTag.putString(DIMENSION_KEY, dimension);
        remarkTag.putDouble(X_KEY, position.x);
        remarkTag.putDouble(Y_KEY, position.y);
        remarkTag.putDouble(Z_KEY, position.z);
        parent.put(REMARK_KEY, remarkTag);
    }

    private static void writeInventoryNode(@NotNull CompoundTag parent, @NotNull ServerPlayer player) {
        CompoundTag inventoryTag = new CompoundTag();

        inventoryTag.put(ARMOR_KEY, ItemStackHelper.Codec.writeSlotsNode(new ListTag(), InventoryHelper.getArmorStacks(player)));
        inventoryTag.put(OFFHAND_KEY, ItemStackHelper.Codec.writeSlotsNode(new ListTag(), InventoryHelper.getOffhandStack(player)));
        inventoryTag.put(ITEM_KEY, ItemStackHelper.Codec.writeSlotsNode(new ListTag(), InventoryHelper.getMainStacks(player)));

        inventoryTag.putInt(SCORE_KEY, player.getScore());
        inventoryTag.putInt(XP_LEVEL_KEY, player.experienceLevel);
        inventoryTag.putFloat(XP_PROGRESS_KEY, player.experienceProgress);
        parent.put(INVENTORY_KEY, inventoryTag);
    }

    private static @NotNull CompoundTag makeDeathNodeNbt(@NotNull ServerPlayer player) {
        CompoundTag node = new CompoundTag();
        writeInventoryNode(node, player);
        writeRemarkNode(node, player);
        return node;
    }

    public @NotNull List<Component> getLore(ServerPlayer player) {
        List<Component> lore = new ArrayList<>();
        lore.add(TextHelper.getTextByKey(player, "deathlog.view.time", this.time));
        lore.add(TextHelper.getTextByKey(player, "deathlog.view.dimension", this.dimension));
        lore.add(TextHelper.getTextByKey(player, "deathlog.view.coordinate", this.x, this.y, this.z));
        lore.add(TextHelper.getTextByKey(player, "deathlog.view.reason", this.reason));
        return lore;
    }
}

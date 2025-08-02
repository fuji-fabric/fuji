package io.github.sakurawald.fuji.module.initializer.deathlog.structure;

import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.InventoryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.NbtHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ItemStackHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.deathlog.DeathLogInitializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
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

    public static DeathNode fromNbt(NbtCompound nbt) {
        DeathNode deathNode = new DeathNode();

        /* Read remark tag. */
        NbtCompound remarkTag = NbtHelper.Primitives.getCompound(nbt, REMARK_KEY).get();

        deathNode.time = NbtHelper.Primitives.getString(remarkTag, TIME_KEY).get();
        deathNode.dimension = NbtHelper.Primitives.getString(remarkTag, DIMENSION_KEY).get();
        deathNode.x = NbtHelper.Primitives.getDouble(remarkTag, X_KEY).get();
        deathNode.y = NbtHelper.Primitives.getDouble(remarkTag, Y_KEY).get();
        deathNode.z = NbtHelper.Primitives.getDouble(remarkTag, Z_KEY).get();
        deathNode.reason = NbtHelper.Primitives.getString(remarkTag, REASON_KEY).get();

        /* Read inventory tag. */
        NbtCompound inventoryNode = NbtHelper.Primitives.getCompound(nbt, INVENTORY_KEY).get();

        // restore main stacks (1*9 slots + 3*9 slots)
        deathNode.main = ItemStackHelper.Codec.readSlotsNode((NbtList) inventoryNode.get(ITEM_KEY));

        deathNode.armor = ItemStackHelper.Codec.readSlotsNode((NbtList) inventoryNode.get(ARMOR_KEY));

        deathNode.offhand = ItemStackHelper.Codec.readSlotsNode((NbtList) inventoryNode.get(OFFHAND_KEY));

        deathNode.score = NbtHelper.Primitives.getInt(inventoryNode, SCORE_KEY).get();
        deathNode.expLevel = NbtHelper.Primitives.getInt(inventoryNode, XP_LEVEL_KEY).get();
        deathNode.expProgress = NbtHelper.Primitives.getFloat(inventoryNode, XP_PROGRESS_KEY).get();

        return deathNode;
    }

    @SneakyThrows
    public static void createDeathNode(@NotNull ServerPlayerEntity player) {
        if (player.getInventory().isEmpty()) return;

        NbtHelper.Storage.withNbtFile(DeathLogInitializer.getDeathDataPath(PlayerHelper.getPlayerName(player)), root -> {
            NbtList deathNodeList = NbtHelper.Walker.getOrCreateNbtElement(root, DEATHS_KEY, new NbtList());
            deathNodeList.add(makeDeathNodeNbt(player));
        });
    }

    private static void writeRemarkNode(@NotNull NbtCompound parent, @NotNull ServerPlayerEntity player) {
        String time = ChronosUtil.Formatter.getFormattedCurrentDate();
        String reason = player.getDamageTracker().getDeathMessage().getString();
        String dimension = player.getWorld().getRegistryKey().getValue().toString();
        Vec3d position = player.getPos();

        NbtCompound remarkTag = new NbtCompound();
        remarkTag.putString(TIME_KEY, time);
        remarkTag.putString(REASON_KEY, reason);
        remarkTag.putString(DIMENSION_KEY, dimension);
        remarkTag.putDouble(X_KEY, position.x);
        remarkTag.putDouble(Y_KEY, position.y);
        remarkTag.putDouble(Z_KEY, position.z);
        parent.put(REMARK_KEY, remarkTag);
    }

    private static void writeInventoryNode(@NotNull NbtCompound parent, @NotNull ServerPlayerEntity player) {
        NbtCompound inventoryTag = new NbtCompound();

        inventoryTag.put(ARMOR_KEY, ItemStackHelper.Codec.writeSlotsNode(new NbtList(), InventoryHelper.getArmorStacks(player)));
        inventoryTag.put(OFFHAND_KEY, ItemStackHelper.Codec.writeSlotsNode(new NbtList(), InventoryHelper.getOffhandStack(player)));
        inventoryTag.put(ITEM_KEY, ItemStackHelper.Codec.writeSlotsNode(new NbtList(), InventoryHelper.getMainStacks(player)));

        inventoryTag.putInt(SCORE_KEY, player.getScore());
        inventoryTag.putInt(XP_LEVEL_KEY, player.experienceLevel);
        inventoryTag.putFloat(XP_PROGRESS_KEY, player.experienceProgress);
        parent.put(INVENTORY_KEY, inventoryTag);
    }

    private static @NotNull NbtCompound makeDeathNodeNbt(@NotNull ServerPlayerEntity player) {
        NbtCompound node = new NbtCompound();
        writeInventoryNode(node, player);
        writeRemarkNode(node, player);
        return node;
    }

    public @NotNull List<Text> getLore(ServerPlayerEntity player) {
        List<Text> lore = new ArrayList<>();
        lore.add(TextHelper.getTextByKey(player, "deathlog.view.time", this.time));
        lore.add(TextHelper.getTextByKey(player, "deathlog.view.dimension", this.dimension));
        lore.add(TextHelper.getTextByKey(player, "deathlog.view.coordinate", this.x, this.y, this.z));
        lore.add(TextHelper.getTextByKey(player, "deathlog.view.reason", this.reason));
        return lore;
    }
}

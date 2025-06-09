package io.github.sakurawald.module.initializer.kit;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.NbtHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.kit.command.argument.wrapper.KitName;
import io.github.sakurawald.module.initializer.kit.gui.KitEditorGui;
import io.github.sakurawald.module.initializer.kit.structure.Kit;
import lombok.SneakyThrows;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandNode("kit")
@CommandRequirement(level = 4)
public class KitInitializer extends ModuleInitializer {

    /* schema keys */
    private static final String INVENTORY = "inventory";

    private static final Path KIT_DATA_DIR_PATH = ReflectionUtil.computeModuleConfigPath(KitInitializer.class).resolve("kit-data");

    public static @NotNull List<String> listKitNames() {
        try (Stream<Path> list = Files.list(KIT_DATA_DIR_PATH)) {
            return list
                .map(it -> it.toFile().getName())
                .toList();
        } catch (IOException e) {
            LogUtil.error("failed to list kits in storage.");
            return Collections.emptyList();
        }
    }

    private static @NotNull List<Kit> readKits() {
        return listKitNames()
            .stream()
            .map(KitInitializer::readKit)
            // ensure the deletion operation on list is supported.
            .collect(Collectors.toList());
    }

    private static Path computePath(String kitName) {
        return KIT_DATA_DIR_PATH.resolve(kitName);
    }

    @SneakyThrows(IOException.class)
    public static void deleteKit(@NotNull String kitName) {
        Files.delete(computePath(kitName));
    }

    public static void writeKit(@NotNull Kit kit) {
        NbtHelper.withNbtFile(computePath(kit.getName()), root -> {
            NbtList nbtList = new NbtList();
            NbtHelper.writeSlotsNode(nbtList, kit.getStackList());
            root.put(INVENTORY, nbtList);
        });
    }

    public static @NotNull Kit readKit(@NotNull String kitName) {
        List<ItemStack> itemStacks = NbtHelper.withNbtFileAndGettingReturnValue(computePath(kitName), root -> {
            /* write empty list if there is no INVENTORY tag */
            if (root.get(INVENTORY) == null) {
                NbtList nbtList = new NbtList();
                root.put(INVENTORY, nbtList);
            }

            /* read slots */
            NbtList nbtList = (NbtList) root.get(INVENTORY);
            return NbtHelper.readSlotsNode(nbtList);
        });

        return new Kit(kitName, itemStacks);
    }

    @CommandNode("editor")
    @Document("Open the kit editor gui.")
    private static int $editor(@CommandSource ServerPlayerEntity player) {
        List<Kit> kits = readKits();
        new KitEditorGui(player, kits, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("give")
    @Document("Give a kit to a player.")
    private static int $give(@CommandSource ServerCommandSource source, ServerPlayerEntity player, KitName kit) {
        /* verify */
        if (Files.notExists(computePath(kit.getValue()))) {
            TextHelper.sendMessageByKey(source, "kit.kit.empty");
            return CommandHelper.Return.FAIL;
        }

        /* read kit*/
        Kit $kit = readKit(kit.getValue());
        insertKit(player, $kit);
        return CommandHelper.Return.SUCCESS;
    }

    private static void insertKit(ServerPlayerEntity player, Kit kit) {
        /* try to insert the item in specified slot */
        PlayerInventory playerInventory = player.getInventory();
        List<ItemStack> tryAgainList = new ArrayList<>();

        for (int i = 0; i < kit.getStackList().size(); i++) {
            ItemStack template = kit.getStackList().get(i);
            if (template.isEmpty()) {
                continue;
            }

            ItemStack copy = template.copy();
            if (!playerInventory.getStack(i).isEmpty() || !playerInventory.insertStack(i, copy)) {
                tryAgainList.add(copy);
            }
        }

        /* try to insert the item in any slot */
        tryAgainList.removeIf(playerInventory::insertStack);

        /* the inventory of player is full, just drop the item in the ground */
        tryAgainList.forEach(it -> player.dropItem(it, true));
    }

    @SneakyThrows(IOException.class)
    @Override
    protected void onInitialize() {
        Files.createDirectories(KIT_DATA_DIR_PATH);
    }

}

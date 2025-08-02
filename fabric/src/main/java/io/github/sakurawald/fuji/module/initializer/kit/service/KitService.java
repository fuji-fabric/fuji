package io.github.sakurawald.fuji.module.initializer.kit.service;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.NbtHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ItemStackHelper;
import io.github.sakurawald.fuji.module.initializer.kit.KitInitializer;
import io.github.sakurawald.fuji.module.initializer.kit.structure.Kit;
import lombok.SneakyThrows;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
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

public class KitService {

    private static final Path KIT_DATA_DIR_PATH = ReflectionUtil.computeModuleConfigPath(KitInitializer.class).resolve("kit-data");
    private static final String INVENTORY_KEY = "inventory";

    @SneakyThrows(IOException.class)
    public static void createKitDirectory() {
        Files.createDirectories(KitService.KIT_DATA_DIR_PATH);
    }

    private static Path getKitPath(String kitName) {
        return KitService.KIT_DATA_DIR_PATH.resolve(kitName);
    }

    public static @NotNull List<String> listKitNames() {
        try (Stream<Path> list = Files.list(KIT_DATA_DIR_PATH)) {
            return list
                .map(it -> it.toFile().getName())
                .toList();
        } catch (IOException e) {
            LogUtil.error("Failed to list kits in storage.");
            return Collections.emptyList();
        }
    }

    public static boolean hasKit(String kitName) {
        return Files.exists(getKitPath(kitName));
    }

    public static @NotNull List<Kit> readKits() {
        return listKitNames()
            .stream()
            .map(KitService::readKit)
            // Create a modifiable list, to ensure the deletion operation on list is supported.
            .collect(Collectors.toList());
    }

    @SneakyThrows(IOException.class)
    public static void deleteKit(@NotNull String kitName) {
        Files.delete(getKitPath(kitName));
    }

    @SneakyThrows
    public static void createKit(@NotNull Kit kit) {
        NbtHelper.Storage.withNbtFile(getKitPath(kit.getName()), root -> {
            NbtList nbtList = new NbtList();
            ItemStackHelper.Codec.writeSlotsNode(nbtList, kit.getStackList());
            LogUtil.debug("createKit: nbtList = {}", nbtList);
            root.put(INVENTORY_KEY, nbtList);
        });
    }

    @SneakyThrows
    public static @NotNull Kit readKit(@NotNull String kitName) {
        List<ItemStack> kitStacks = NbtHelper.Storage.withNbtFile(getKitPath(kitName), root -> {
            /* Write empty list if there is no INVENTORY tag. */
            if (root.get(INVENTORY_KEY) == null) {
                root.put(INVENTORY_KEY, new NbtList());
            }

            /* Read slots from inventory tag. */
            NbtList nbtList = (NbtList) root.get(INVENTORY_KEY);
            return ItemStackHelper.Codec.readSlotsNode(nbtList);
        });

        return new Kit(kitName, kitStacks);
    }

    public static void giveKit(ServerPlayerEntity player, Kit kit) {
        PlayerInventory playerInventory = player.getInventory();
        List<ItemStack> tryAgainList = new ArrayList<>();

        /* Enumerate the kit items. */
        for (int i = 0; i < kit.getStackList().size(); i++) {
            ItemStack template = kit.getStackList().get(i);

            if (template.isEmpty() || GuiHelper.Validator.isBannedSlotPlaceholder(template)) {
                continue;
            }

            /* Try to insert the item in specified slot. */
            ItemStack copy = template.copy();
            if (!playerInventory.getStack(i).isEmpty() || !playerInventory.insertStack(i, copy)) {
                tryAgainList.add(copy);
            }
        }

        /* Try to insert the item in any slot. */
        tryAgainList.removeIf(playerInventory::insertStack);

        /* The inventory of player is full, just drop the item in the ground */
        tryAgainList.forEach(it -> player.dropItem(it, true));
    }

}

package io.github.sakurawald.fuji.module.initializer.deathlog;

import io.github.sakurawald.fuji.core.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.deathlog.gui.DeathDataListGui;
import lombok.SneakyThrows;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


@Document("""
    This module stores the `inventory` as death log on player death.
    """)
public class DeathLogInitializer extends ModuleInitializer {

    private static final Path DEATH_DATA_DIR_PATH = ReflectionUtil.computeModuleConfigPath(DeathLogInitializer.class).resolve("death-data");

    public static @NotNull Path getDeathDataPath(String playerName) {
        String fileName = Uuids.getOfflinePlayerUuid(playerName) + ".dat";
        return DEATH_DATA_DIR_PATH.resolve(fileName);
    }

    @Document("Open the `deathlog` GUI.")
    @CommandNode("deathlog")
    @CommandRequirement(level = 4)
    private static int $gui(@CommandSource ServerPlayerEntity player) {
        List<String> offlinePlayerNames = ServerHelper.getOfflinePlayerNames();
        new DeathDataListGui(player, offlinePlayerNames, 0)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @SneakyThrows(IOException.class)
    @Override
    protected void onInitialize() {
        Files.createDirectories(DEATH_DATA_DIR_PATH);
    }

}

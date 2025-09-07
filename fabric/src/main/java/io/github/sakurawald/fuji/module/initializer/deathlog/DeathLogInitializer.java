package io.github.sakurawald.fuji.module.initializer.deathlog;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.player.PlayerDeathEvent;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.deathlog.gui.DeathDataListGui;
import io.github.sakurawald.fuji.module.initializer.deathlog.structure.DeathNode;
import lombok.SneakyThrows;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


@Document(id = 1751826834086L, value = """
    This module stores the `inventory` as death log on player death.
    """)
public class DeathLogInitializer extends ModuleInitializer {

    private static final Path DEATH_DATA_DIR_PATH = ReflectionUtil.computeModuleConfigPath(DeathLogInitializer.class).resolve("death-data");

    public static @NotNull Path getDeathDataPath(String playerName) {
        String fileName = Uuids.getOfflinePlayerUuid(playerName) + ".dat";
        return DEATH_DATA_DIR_PATH.resolve(fileName);
    }

    @Document(id = 1751826836196L, value = "Open the `deathlog` GUI.")
    @CommandNode("deathlog")
    @CommandRequirement(level = 4)
    private static int $gui(@CommandSource ServerPlayerEntity player) {
        List<String> offlinePlayerNames = PlayerHelper.Cache.getOfflinePlayerNames();
        new DeathDataListGui(player, offlinePlayerNames, 0)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @SneakyThrows(IOException.class)
    @Override
    protected void onInitialize() {
        Files.createDirectories(DEATH_DATA_DIR_PATH);
    }

    @EventConsumer
    private static void handleOnPlayerDeathEvent(PlayerDeathEvent event) {
        DeathNode.createDeathNode(event.getPlayer());
    }
}

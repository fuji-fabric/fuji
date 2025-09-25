package mod.fuji.module.initializer.deathlog;

import mod.fuji.core.annotation.Unused;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.ReflectionUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerDeathEvent;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.deathlog.gui.DeathDataListGui;
import mod.fuji.module.initializer.deathlog.structure.DeathNode;
import lombok.SneakyThrows;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


@Document(id = 1751826834086L, value = """
    This module stores a player's inventory as a `death log` upon death.
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

    @EventConsumer
    @SneakyThrows(IOException.class)
    private static void onServerStarted(@Unused ServerStartedEvent event) {
        Files.createDirectories(DEATH_DATA_DIR_PATH);
    }

    @EventConsumer
    private static void handleOnPlayerDeathEvent(PlayerDeathEvent event) {
        DeathNode.createDeathNode(event.getPlayer());
    }
}

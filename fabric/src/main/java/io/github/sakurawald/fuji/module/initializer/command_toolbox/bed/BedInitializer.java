package io.github.sakurawald.fuji.module.initializer.command_toolbox.bed;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;


public class BedInitializer extends ModuleInitializer {

    @Document(id = 1751825169906L, value = "Teleport to the bed location.")
    @CommandNode("bed")
    private static int $bed(@CommandSource @CommandTarget ServerPlayerEntity player) {
        return WorldHelper.SpawnPos
            .getPlayerSpawnPos(player)
            .map(playerSpawnPos -> {
                playerSpawnPos.teleport(player);
                TextHelper.sendTextByKey(player, "bed.success");
                return CommandHelper.Return.SUCCESS;
            })
            .orElseGet(() -> {
                TextHelper.sendTextByKey(player, "bed.not_found");
                return CommandHelper.Return.FAILURE;
            });

    }

}

package io.github.sakurawald.fuji.module.initializer.command_toolbox.near;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.Optional;

public class NearInitializer extends ModuleInitializer {

    private static int distance(ServerPlayerEntity a, ServerPlayerEntity b) {
        if (EntityHelper.getServerWorld(a) != EntityHelper.getServerWorld(b)) return Integer.MAX_VALUE;
        return (int) a.getBlockPos().getSquaredDistance(b.getBlockPos().toCenterPos());
    }

    @Document(id = 1751825090796L, value = "List nearby players.")
    @CommandNode("near")
    @CommandRequirement(level = 4)
    private static int $near(@CommandSource ServerPlayerEntity player, Optional<Integer> distance) {
        int $distance = distance.orElse(128);

        int sd = $distance * $distance;
        List<String> result = PlayerHelper.Lookup.getOnlinePlayers()
            .stream()
            .filter(p -> !p.equals(player) && distance(player, p) <= sd)
            .map(PlayerHelper::getPlayerName)
            .toList();

        TextHelper.sendTextByKey(player, "near.format", result);
        return CommandHelper.Return.SUCCESS;
    }

}

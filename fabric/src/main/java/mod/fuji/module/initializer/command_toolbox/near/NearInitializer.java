package mod.fuji.module.initializer.command_toolbox.near;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;

public class NearInitializer extends ModuleInitializer {

    private static int distance(ServerPlayer a, ServerPlayer b) {
        if (EntityHelper.getServerWorld(a) != EntityHelper.getServerWorld(b)) return Integer.MAX_VALUE;
        return (int) a.blockPosition().distToCenterSqr(b.blockPosition().getCenter());
    }

    @Document(id = 1751825090796L, value = "List nearby players.")
    @CommandNode("near")
    @CommandRequirement(level = 4)
    private static int $near(@CommandSource ServerPlayer player, Optional<Integer> distance) {
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

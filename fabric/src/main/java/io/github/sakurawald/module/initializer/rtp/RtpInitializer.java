package io.github.sakurawald.module.initializer.rtp;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.annotation.CommandTarget;
import io.github.sakurawald.core.command.argument.wrapper.impl.Dimension;
import io.github.sakurawald.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.service.random_teleport.RandomTeleporter;
import io.github.sakurawald.core.structure.TeleportSetup;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.rtp.config.model.RtpConfigModel;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class RtpInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<RtpConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, RtpConfigModel.class);

    private static @NotNull TeleportSetup withTeleportSetup(@NotNull ServerPlayerEntity player, @NotNull ServerWorld world) {
        List<TeleportSetup> list = config.model().setup.dimension;
        String dimension = RegistryHelper.ofString(world);

        Optional<TeleportSetup> first = list.stream().filter(o -> o.getDimension().equals(dimension)).findFirst();
        if (first.isEmpty()) {
            TextHelper.sendMessageByKey(player, "rtp.dimension.disallow", RegistryHelper.ofString(world));
            throw new AbortCommandExecutionException();
        }

        return first.get();
    }

    @CommandNode("rtp")
    @Document("Random rtp in specified dimension.")
    private static int $rtp(@CommandSource @CommandTarget ServerPlayerEntity player, Optional<Dimension> dimension) {
        ServerWorld serverWorld = dimension.isPresent() ? dimension.get().getValue() : player.getServerWorld();
        TeleportSetup setup = withTeleportSetup(player, serverWorld);

        TextHelper.sendActionBarByKey(player, "rtp.tip");
        RandomTeleporter.request(player, setup, (position) -> TextHelper.sendMessageByKey(player, "rtp.success"));
        return CommandHelper.Return.SUCCESS;
    }
}

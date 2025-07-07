package io.github.sakurawald.fuji.module.initializer.rtp;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.Dimension;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.service.random_teleport.RandomTeleporter;
import io.github.sakurawald.fuji.core.structure.TeleportSetup;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.rtp.config.model.RtpConfigModel;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

@Document(id = 1751826337744L, value = """
    Provides random teleportation.
    """)
public class RtpInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<RtpConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, RtpConfigModel.class);

    private static @NotNull TeleportSetup withTeleportSetup(@NotNull ServerPlayerEntity player, @NotNull ServerWorld world) {
        List<TeleportSetup> list = config.model().setup.dimension;
        String dimension = RegistryHelper.toString(world);

        Optional<TeleportSetup> first = list.stream().filter(o -> o.getDimension().equals(dimension)).findFirst();
        if (first.isEmpty()) {
            TextHelper.sendTextByKey(player, "rtp.dimension.disallow", RegistryHelper.toString(world));
            throw new AbortCommandExecutionException();
        }

        return first.get();
    }

    @Document(id = 1751826340406L, value = "Random rtp in specified dimension.")
    @CommandNode("rtp")
    private static int $rtp(@CommandSource @CommandTarget ServerPlayerEntity player, Optional<Dimension> dimension) {
        ServerWorld serverWorld = dimension.isPresent() ? dimension.get().getValue() : EntityHelper.getServerWorld(player);
        TeleportSetup setup = withTeleportSetup(player, serverWorld);

        TextHelper.sendTextByKey(player, "rtp.tip");
        RandomTeleporter.request(player, setup, (position) -> TextHelper.sendTextByKey(player, "rtp.success"));
        return CommandHelper.Return.SUCCESS;
    }
}

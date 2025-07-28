package io.github.sakurawald.fuji.module.initializer.jail;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.jail.config.model.JailConfigModel;
import io.github.sakurawald.fuji.module.initializer.jail.config.model.JailDataModel;
import io.github.sakurawald.fuji.module.initializer.jail.service.JailService;
import io.github.sakurawald.fuji.module.initializer.jail.structure.JailDescriptor;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@Document(id = 1753681022357L, value = """
    This module allows you to define a `jail`.
    It can be used to `punish` a player with bad behaviour, without `banning` it.
    """)
public class JailInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<JailConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, JailConfigModel.class);

    public static final BaseConfigurationHandler<JailDataModel> data = new ObjectConfigurationHandler<>("jail-data.json", JailDataModel.class);

    @Document(id = 1753686048373L, value = "List all defined `jails`.")
    @CommandNode("jail list")
    @CommandRequirement(level = 4)
    private static int $list(@CommandSource ServerCommandSource source) {
        List<String> jailIds = JailService
            .getJailDescriptors()
            .stream()
            .map(JailDescriptor::getId)
            .toList();

        TextHelper.sendTextByKey(source, "jail.list", jailIds);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1753686063844L, value = "Put the `player` into a specified `jail`.")
    @CommandNode("jail put")
    @CommandRequirement(level = 4)
    private static int $put(@CommandSource ServerCommandSource source, ServerPlayerEntity player, JailDescriptor jail, String reason, Optional<String> duration) {


        return CommandHelper.Return.SUCCESS;
    }

}

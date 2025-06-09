package io.github.sakurawald.module.initializer.gameplay.carpet.better_info;

import io.github.sakurawald.core.event.impl.CommandEvents;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.command.CommandManager;

import java.util.List;

public class BetterInfoInitializer extends ModuleInitializer {

    @Override
    protected void onInitialize() {
        CommandEvents.REGISTRATION.register((dispatcher, registryAccess, environment) -> dispatcher.register(
            CommandManager.literal("info").then(
                dispatcher.findNode(List.of("data", "get", "entity"))
            )
        ));
    }

}

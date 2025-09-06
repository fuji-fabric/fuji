package io.github.sakurawald.fuji.module.initializer.gameplay.carpet.better_info;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.message.impl.CommandEvents;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.CommandManager;

import java.util.List;

@Document(id = 1751827014561L, value = """
    1. Provides the `/info entity` command.
    2. Adds `block entity` query for `/info block` command.
    """)
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

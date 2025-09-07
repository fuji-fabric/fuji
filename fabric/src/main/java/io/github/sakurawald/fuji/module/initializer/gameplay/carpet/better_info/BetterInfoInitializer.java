package io.github.sakurawald.fuji.module.initializer.gameplay.carpet.better_info;

import com.mojang.brigadier.CommandDispatcher;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.server.command.OnCommandRegistrationEvent;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.CommandManager;

import java.util.List;
import net.minecraft.server.command.ServerCommandSource;

@Document(id = 1751827014561L, value = """
    1. Provides the `/info entity` command.
    2. Adds `block entity` query for `/info block` command.
    """)
public class BetterInfoInitializer extends ModuleInitializer {


    @EventConsumer
    private static void onCommandRegistrationEvent(OnCommandRegistrationEvent event) {
        CommandDispatcher<ServerCommandSource> dispatcher = event.getDispatcher();
        dispatcher.register(
            CommandManager.literal("info").then(
                dispatcher.findNode(List.of("data", "get", "entity"))
            )
        );
    }

}

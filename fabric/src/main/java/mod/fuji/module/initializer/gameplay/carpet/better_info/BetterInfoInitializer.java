package mod.fuji.module.initializer.gameplay.carpet.better_info;

import com.mojang.brigadier.CommandDispatcher;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.command.CommandRegistrationEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.commands.Commands;

import java.util.List;
import net.minecraft.commands.CommandSourceStack;

@Document(id = 1751827014561L, value = """
    1. Provides the `/info entity` command.
    2. Adds `block entity` query for `/info block` command.
    """)
public class BetterInfoInitializer extends ModuleInitializer {


    @EventConsumer
    private static void onCommandRegistrationEvent(CommandRegistrationEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
            Commands.literal("info").then(
                dispatcher.findNode(List.of("data", "get", "entity"))
            )
        );
    }

}

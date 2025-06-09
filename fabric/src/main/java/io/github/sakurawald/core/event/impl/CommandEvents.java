package io.github.sakurawald.core.event.impl;

import com.mojang.brigadier.CommandDispatcher;
import io.github.sakurawald.core.event.abst.Event;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class CommandEvents {

    public static final Event<CommandRegistrationCallback> REGISTRATION = new Event<>((listeners) -> (d, r, e) -> {
        for (CommandRegistrationCallback listener : listeners) {
            listener.fire(d, r, e);
        }
    });


    public interface CommandRegistrationCallback {
        void fire(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment);
    }
}

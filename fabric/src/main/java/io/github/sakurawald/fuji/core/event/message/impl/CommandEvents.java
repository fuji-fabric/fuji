package io.github.sakurawald.fuji.core.event.message.impl;

import com.mojang.brigadier.CommandDispatcher;
import io.github.sakurawald.fuji.core.event.message.abst.SimpleEvent;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class CommandEvents {

    public static final SimpleEvent<AfterCommandRegistrationCallback> AFTER_REGISTRATION = new SimpleEvent<>((listeners) -> (m, d, r, e) -> listeners.forEach(listener -> listener.fire(m, d, r, e)));


    public interface AfterCommandRegistrationCallback {

        void fire(CommandManager commandManager, CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment);
    }
}

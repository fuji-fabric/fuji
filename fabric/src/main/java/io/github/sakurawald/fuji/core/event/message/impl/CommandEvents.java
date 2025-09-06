package io.github.sakurawald.fuji.core.event.message.impl;

import com.mojang.brigadier.CommandDispatcher;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.event.message.abst.SimpleEvent;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class CommandEvents {

    @ForDeveloper("""
        The `/reload` command in vanilla Minecraft will clear the root command tree.
        You have to hook this event, to ensure your registered commands still exist after the `/reload` command.
        """)
    public static final SimpleEvent<CommandRegistrationCallback> REGISTRATION = new SimpleEvent<>((listeners) -> (d, r, e) -> listeners.forEach(listener -> listener.fire(d, r, e)));

    public static final SimpleEvent<AfterCommandRegistrationCallback> AFTER_REGISTRATION = new SimpleEvent<>((listeners) -> (m, d, r, e) -> listeners.forEach(listener -> listener.fire(m, d, r, e)));

    public interface CommandRegistrationCallback {
        void fire(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment);

    }

    public interface AfterCommandRegistrationCallback {

        void fire(CommandManager commandManager, CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment);
    }
}

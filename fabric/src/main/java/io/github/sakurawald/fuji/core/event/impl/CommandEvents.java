package io.github.sakurawald.fuji.core.event.impl;

import com.mojang.brigadier.CommandDispatcher;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.event.abst.Event;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class CommandEvents {

    @ForDeveloper("""
        The `/reload` command in vanilla Minecraft will clear the root command tree.
        You have to hook this event, to ensure your registered commands still exist after the `/reload` command.
        """)
    public static final Event<CommandRegistrationCallback> REGISTRATION = new Event<>((listeners) -> (d, r, e) -> listeners.forEach(listener -> listener.fire(d, r, e)));

    public static final Event<CommandRegistrationCallback> AFTER_REGISTRATION = new Event<>((listeners) -> (d, r, e) -> listeners.forEach(listener -> listener.fire(d, r, e)));

    public interface CommandRegistrationCallback {
        void fire(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment);
    }
}

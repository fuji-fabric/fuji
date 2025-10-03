package mod.fuji.core.event.message.server.command;

import com.mojang.brigadier.CommandDispatcher;
import mod.fuji.core.event.message.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

/**
 *     The <code>/reload</code> command in vanilla Minecraft will clear the root command tree.
    You have to hook this event, to ensure your registered commands still exist after the <code>/reload</code> command.
    Note that during the initialization of CommandManager, the MinecraftServer reference is null.

 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class CommandRegistrationEvent extends BaseEvent {
    @NotNull CommandManager commandManager;
    @NotNull CommandDispatcher<ServerCommandSource> dispatcher;
    @NotNull CommandRegistryAccess registryAccess;
    @NotNull CommandManager.RegistrationEnvironment environment;
}

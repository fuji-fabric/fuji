package mod.fuji.core.event.message.server.command;

import com.mojang.brigadier.CommandDispatcher;
import mod.fuji.core.event.message.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

/**
 *     The <code>/reload</code> command in vanilla Minecraft will clear the root command tree.
    You have to hook this event, to ensure your registered commands still exist after the <code>/reload</code> command.
    Note that during the initialization of CommandManager, the MinecraftServer reference is null.

 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class CommandRegistrationEvent extends BaseEvent {
    @NotNull Commands commandManager;
    @NotNull CommandDispatcher<CommandSourceStack> dispatcher;
    @NotNull CommandBuildContext registryAccess;
    @NotNull Commands.CommandSelection environment;
}

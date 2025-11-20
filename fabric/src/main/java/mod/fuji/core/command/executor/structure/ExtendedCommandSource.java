package mod.fuji.core.command.executor.structure;

import lombok.Value;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.descriptor.CommandDescriptor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;


/**
 * Cases:
 * <p>
 * 1. A command is initialized by player Alice, and executed as player Bob.
 * <p>
 * 2. A command is initialized by player Alice, and executed as the console. (/run as console)
 * <p>
 * 3. A command is initialized by the console, and executed as the console. (command scheduler)
 * <p>
 * 4. A command is initialized by a player, and executed as the player. (interactive sign)
 **/
@Value
public class ExtendedCommandSource {

    @NotNull CommandSourceStack initiatingSource;
    @NotNull CommandSourceStack executingSource;
    boolean parsePlaceholders;

    public ExtendedCommandSource(@NotNull CommandSourceStack initiatingSource, @NotNull CommandSourceStack executingSource, boolean parsePlaceholders) {
        this.initiatingSource = initiatingSource;

        if (CommandDescriptor.silentSpecialVariable.get()) {
            this.executingSource = executingSource.withSuppressedOutput();
        } else {
            this.executingSource = executingSource;
        }

        this.parsePlaceholders = parsePlaceholders;
    }

    public static ExtendedCommandSource fromSource(@NotNull CommandSourceStack initiatingSource) {
        return new ExtendedCommandSource(initiatingSource, initiatingSource, true);
    }

    public static ExtendedCommandSource asConsole(@NotNull CommandSourceStack initiatingSource) {
        return new ExtendedCommandSource(initiatingSource, CommandHelper.Source.getConsoleCommandSource(), true);
    }

    public static ExtendedCommandSource asPlayer(@NotNull CommandSourceStack initiatingSource, ServerPlayer executingPlayer) {
        return new ExtendedCommandSource(initiatingSource, CommandHelper.Source.getCommandSource(executingPlayer), true);
    }

    public static ExtendedCommandSource asFakeOp(@NotNull CommandSourceStack initiatingSource, ServerPlayer executingPlayer) {
        return new ExtendedCommandSource(initiatingSource, CommandHelper.Source.getCommandSource(executingPlayer).withPermission(4), true);
    }

    public boolean sameSource() {
        return executingSource.getTextName().equals(initiatingSource.getTextName());
    }

    private CommandSourceStack getCommandSourceForPlaceholderParsing() {
        // NOTE: Use the deepest command source to parse placeholders.
        if (executingSource.isPlayer()) {
            return executingSource;
        }

        return initiatingSource;
    }

    public String expandCommand(String string) {
        /* Parse placeholders. */
        if (!this.parsePlaceholders) return string;

        ServerPlayer contextualPlayer = getCommandSourceForPlaceholderParsing().getPlayer();
        if (contextualPlayer != null) {
            string = TextHelper.Parsers.parsePlaceholderString(contextualPlayer, string);
        } else {
            string = TextHelper.Parsers.parsePlaceholderString(ServerHelper.getServer(), string);
        }

        return string;
    }

    @Override
    public String toString() {
        return "ExtendedCommandSource{" +
            "initiatingSource=" + initiatingSource.getTextName() +
            ", executingSource=" + executingSource.getTextName() +
            ", parsePlaceholder=" + parsePlaceholders +
            '}';
    }
}

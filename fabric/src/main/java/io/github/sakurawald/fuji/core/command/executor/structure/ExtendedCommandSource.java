package io.github.sakurawald.fuji.core.command.executor.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;


@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ForDeveloper("""
    Cases:
    1. A command is initialized by player Alice, and executed as player Bob.
    2. A command is initialized by player Alice, and executed as the console. (/run as console)
    3. A command is initialized by the console, and executed as the console. (command scheduler)
    4. A command is initialized by a player, and executed as the player. (interactive sign)
    """)
public class ExtendedCommandSource {

    @NotNull ServerCommandSource initiatingSource;
    @NotNull ServerCommandSource executingSource;
    boolean parsePlaceholder;

    public static ExtendedCommandSource fromSource(@NotNull ServerCommandSource initiatingSource) {
        return new ExtendedCommandSource(initiatingSource, initiatingSource, true);
    }

    public static ExtendedCommandSource asConsole(@NotNull ServerCommandSource initiatingSource) {
        return new ExtendedCommandSource(initiatingSource, ServerHelper.getServer().getCommandSource(), true);
    }

    public static ExtendedCommandSource asPlayer(@NotNull ServerCommandSource initiatingSource, ServerPlayerEntity executingPlayer) {
        return new ExtendedCommandSource(initiatingSource, executingPlayer.getCommandSource(), true);
    }

    public static ExtendedCommandSource asFakeOp(@NotNull ServerCommandSource initiatingSource, ServerPlayerEntity executingPlayer) {
        return new ExtendedCommandSource(initiatingSource, executingPlayer.getCommandSource().withLevel(4), true);
    }

    public boolean sameSource() {
        return executingSource.getName().equals(initiatingSource.getName());
    }

    private ServerCommandSource getCommandSourceForPlaceholderParsing() {
        // NOTE: Use the deepest command source to parse placeholders.
        if (executingSource.isExecutedByPlayer()) {
            return executingSource;
        }

        return initiatingSource;
    }

    public String expandCommand(String string) {
        /* Parse placeholders. */
        if (!this.parsePlaceholder) return string;

        ServerPlayerEntity contextualPlayer = getCommandSourceForPlaceholderParsing().getPlayer();
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
            "initiatingSource=" + initiatingSource.getName() +
            ", executingSource=" + executingSource.getName() +
            ", parsePlaceholder=" + parsePlaceholder +
            '}';
    }
}

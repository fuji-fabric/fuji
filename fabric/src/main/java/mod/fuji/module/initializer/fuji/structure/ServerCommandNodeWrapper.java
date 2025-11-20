package mod.fuji.module.initializer.fuji.structure;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.CommandNode;
import mod.fuji.core.command.structure.CommandNodeWithPath;
import net.minecraft.commands.CommandSourceStack;

public class ServerCommandNodeWrapper extends CommandNodeWithPath {

    private static final String UNKNOWN_PACKAGE = "unknown";

    public final String fromPackage;

    public ServerCommandNodeWrapper(CommandNode<CommandSourceStack> node) {
        super(node);
        this.fromPackage = guessWhichPackageTheCommandIsFrom(node);
    }

    private String guessWhichPackageTheCommandIsFrom(CommandNode<?> commandNode) {
        // NOTE: In this solution, we only go deeper to find the possible package.
        // For some commands that redirect to its parent node, this solution fails.

        /* Try to find the package from CommandNode. */
        Command<?> commandFunction = commandNode.getCommand();
        if (commandFunction != null) {
            return commandFunction.getClass().getPackage().getName();
        }

        /* Try to find the package from CommandNode#getRedirect. */
        CommandNode<?> redirectNode = commandNode.getRedirect();
        if (redirectNode != null) {
            commandFunction = redirectNode.getCommand();
            if (commandFunction != null) {
                return commandFunction.getClass().getPackage().getName();
            }
        }

        /* We need to go deeper. */
        // NOTE: We failed to guess the package for some commands, like `/execute run` command.
        for (CommandNode<?> child : commandNode.getChildren()) {
            String result = guessWhichPackageTheCommandIsFrom(child);
            if (!result.equals(UNKNOWN_PACKAGE)) {
                return result;
            }
        }

        /* Okay, let's abort it. */
//        LogUtil.debug("Failed to guess the package name for command node {}", commandNode);
        return UNKNOWN_PACKAGE;
    }

}

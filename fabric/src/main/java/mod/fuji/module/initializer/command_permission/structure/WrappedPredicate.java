package mod.fuji.module.initializer.command_permission.structure;

import com.mojang.brigadier.tree.CommandNode;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.module.initializer.command_permission.CommandPermissionInitializer;
import mod.fuji.module.initializer.command_permission.service.CommandPermissionService;
import java.util.function.Predicate;
import net.luckperms.api.util.Tristate;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

@TestCase(action = "Issue `/fuji reload` and `/reload` commands in `neoforge single player world`.", targets = {
    "It should not trigger the Concurrent Modification Exception."
})
public class WrappedPredicate<T> implements Predicate<T> {

    final CommandNode<ServerCommandSource> commandNode;
    final Predicate<Object> originalRequirement;
    String commandPath;

    public WrappedPredicate(CommandNode<ServerCommandSource> commandNode, Predicate<Object> originalRequirement) {
        this.commandNode = commandNode;
        this.originalRequirement = originalRequirement;
    }

    public String getCachedCommandPath() {
        if (this.commandPath == null) {
            this.commandPath = CommandHelper.Tree.findCommandNodePath(this.commandNode);
        }

        return this.commandPath;
    }

    @Override
    public boolean test(@NotNull Object commandSource) {
        /* If the command source is client command source, use the original predicate. */
        if (commandSource instanceof ServerCommandSource serverCommandSource) {
            try {
                /* Ignore the non-player command source. */
                if (serverCommandSource.getPlayer() == null) {
                    return this.originalRequirement.test(commandSource);
                }

                /* Compute the command node path. */
                String commandPath = this.getCachedCommandPath();

                /* Ask the pre-defined rules if the player can use the command. */
                if (!CommandHelper.Requirement.isAdmin(serverCommandSource)) {
                    String requiredPermissionToExecuteThisCommand = CommandPermissionInitializer.COMMAND_PERMISSION_UNIFIED_PERMISSION.withArguments(commandPath);
                    for (CommandPermissionRule rule : CommandPermissionInitializer.config.model().rules) {
                        if (requiredPermissionToExecuteThisCommand.matches(rule.permissionPatternRegex)) {
                            Tristate predefinePermissionTestResult = rule.permissionTestResult.toTriState();
                            CommandPermissionService.processVerboseModeFeature("PREDEFINED RULES", serverCommandSource, commandPath, predefinePermissionTestResult);

                            return CommandPermissionService.canUseThisCommand(serverCommandSource, predefinePermissionTestResult, this.originalRequirement);
                        }
                    }
                }

                /* Ask luckperms if the player can use the command. */
                Tristate luckpermsPermissionTestResult = LuckpermsHelper.getPermission(serverCommandSource.getPlayer().getUuid(), CommandPermissionInitializer.COMMAND_PERMISSION_UNIFIED_PERMISSION, commandPath);
                CommandPermissionService.processVerboseModeFeature("LUCKPERMS", serverCommandSource, commandPath, luckpermsPermissionTestResult);

                return CommandPermissionService.canUseThisCommand(serverCommandSource, luckpermsPermissionTestResult, this.originalRequirement);
            } catch (Throwable useOriginalPredicateIfFailed) {
                LogUtil.error("Failed to test the command requirement using WrappedPredicate, falling back to original predicate. (command node = {}, source = {})", this.commandNode, serverCommandSource, useOriginalPredicateIfFailed);
                return this.originalRequirement.test(commandSource);
            }
        } else {
            /* The command source is not ServerCommandSource, simply use the original requirement. */
            return this.originalRequirement.test(commandSource);
        }
    }

}

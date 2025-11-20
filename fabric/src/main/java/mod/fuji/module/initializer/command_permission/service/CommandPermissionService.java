package mod.fuji.module.initializer.command_permission.service;

import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.module.initializer.command_permission.structure.WrappedPredicate;
import java.util.function.Predicate;
import net.luckperms.api.util.Tristate;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class CommandPermissionService {
    public static boolean verboseModeFlag = false;

    public static void processVerboseModeFeature(String askWhoForPermissionTestResult, CommandSourceStack source, String commandPath, Tristate commandPermissionTestResult) {
        if (!verboseModeFlag) return;

        // Make description.
        String explanationForPermissionTestResult = makeExplanationForPermissionTestResult(commandPermissionTestResult);

        // Info in console.
        LogUtil.info("""

            ◉ Command Source: {}
            ◉ Command Path of the Target Command: {}
            ◉ Ask who for permission test result: {}
            ◉ Permission Test Result: {}
            ◉ Explanation: {}
            """, source.getTextName(), commandPath, askWhoForPermissionTestResult, commandPermissionTestResult, explanationForPermissionTestResult);
    }

    private static @NotNull String makeExplanationForPermissionTestResult(Tristate state) {
        String explanation;
        if (state == Tristate.UNDEFINED) {
            explanation = "The permission test result is UNDEFINED, it means command_permission module WILL NOT HANDLE this command. We simply fallback the requirement predicate of this command to its original predicate.";
        } else if (state == Tristate.TRUE) {
            explanation = "The permission test result is TRUE, it means command_permission module WILL ALLOW the command source to use this command.";
        } else if (state == Tristate.FALSE) {
            explanation = "The permission test result is FALSE, it means command_permission module WILL DIS-ALLOW the command source to use this command.";
        } else {
            explanation = "I don't know why, but the value of Tristate is un-expected.";
        }
        return explanation;
    }

    public static @NotNull WrappedPredicate<Object> makeWrappedPredicate(@NotNull com.mojang.brigadier.tree.CommandNode<CommandSourceStack> commandNode, @NotNull Predicate<Object> originalRequirement) {
        return new WrappedPredicate<>(commandNode, originalRequirement);
    }

    public static boolean canUseThisCommand(CommandSourceStack source, Tristate permissionTestResult, @NotNull Predicate<Object> originalRequirement) {
        /* If the corresponding permission is DEFINED, we use it to override the original requirement. */
        if (permissionTestResult != Tristate.UNDEFINED) {
            return permissionTestResult.asBoolean();
        }

        /* If the corresponding permission is UNDEFINED, we just fall back to original predicate. */
        return originalRequirement.test(source);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void ensureCommandNodeRequirementIsWrapped() {
        // Enumerate all registered commands, to ensure the getRequirement() is triggered. (For luckperms permission cache)
        LogUtil.debug("Ensure command nodes are wrapped.");
        CommandHelper.Tree
            .getAllCommandNodes()
            .forEach(com.mojang.brigadier.tree.CommandNode::getRequirement);
    }

    public static boolean isCommandNodeWrapped(com.mojang.brigadier.tree.CommandNode<CommandSourceStack> commandNode) {
        return commandNode.getRequirement() instanceof WrappedPredicate<CommandSourceStack>;
    }
}

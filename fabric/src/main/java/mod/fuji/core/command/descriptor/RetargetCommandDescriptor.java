package mod.fuji.core.command.descriptor;

import com.mojang.brigadier.Command;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.argument.wrapper.impl.PlayerCollection;
import mod.fuji.core.command.structure.CommandRequirementDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class RetargetCommandDescriptor extends CommandDescriptor {

    public static final String OTHERS_LITERAL = "others";
    private final int commandSourceParameterIndex;
    private final int commandTargetParameterIndex;
    private final int othersParameterIndex;

    private RetargetCommandDescriptor(@NotNull CommandDescriptor baseCommandDescriptor) {
        super(baseCommandDescriptor.method, makeRetargetCommandArguments(baseCommandDescriptor));

        /* Find command target parameter index. */
        this.commandTargetParameterIndex = baseCommandDescriptor.findCommandTargetMethodParameterSpecifierIndex()
            .orElseThrow(() -> new IllegalArgumentException("Failed to find the command target parameter index in command descriptor %s".formatted(this)));

        /* Find the others parameter index. */
        this.othersParameterIndex = this.findOthersParameterIndex()
            .orElseThrow(() -> new IllegalStateException("Failed to find the others parameter index in command descriptor %s".formatted(this)));

        /* Find command source parameter index. */
        this.commandSourceParameterIndex = this.findCommandSourceMethodParameterSpecifierIndex()
            .orElseThrow(() -> new IllegalStateException("Failed to find the command source parameter index in command descriptor %s".formatted(this)));

        /* Copy the document from the base command descriptor. */
        this.fillDocument(baseCommandDescriptor.document);
    }

    public static Optional<RetargetCommandDescriptor> from(@NotNull CommandDescriptor commandDescriptor) {
        /* Filter the method that contains @CommandTarget annotation. */
        Optional<Integer> commandTargetParameterIndex = commandDescriptor.findCommandTargetMethodParameterSpecifierIndex();
        return commandTargetParameterIndex
            .map($commandTargetParameterIndex -> new RetargetCommandDescriptor(commandDescriptor));
    }

    private Optional<Integer> findOthersParameterIndex() {
        return findMethodParameterSpecifierIndex(commandArgument ->
            commandArgument.getArgumentName().equals(OTHERS_LITERAL)
                && commandArgument.isRequiredArgument());
    }

    private static @NotNull List<CommandArgument> makeRetargetCommandArguments(@NotNull CommandDescriptor commandDescriptor) {
        /* Make the result command arguments. */
        List<CommandArgument> retargetCommandArguments = new ArrayList<>(commandDescriptor.commandArguments);

        /* Find the command target argument index. */
        int commandTargetArgumentIndex = commandDescriptor.findCommandArgumentIndex(CommandArgument::isCommandTarget)
            .orElseThrow(() -> new IllegalStateException("Failed to find the target argument index in command descriptor %s".formatted(commandDescriptor)));
        CommandArgument commandTargetArgument = retargetCommandArguments.get(commandTargetArgumentIndex);

        /* All the retarget commands require level 4 permission to use. */
        CommandRequirementDescriptor requirement = new CommandRequirementDescriptor(4, null);

        /* If the argument annotated with @CommandTarget is also annotated with @CommandSource, then split the @CommandSource argument and @CommandTarget argument. */
        if (commandTargetArgument.isCommandSource()) {
            retargetCommandArguments.add(commandTargetArgumentIndex + 1, CommandArgument.ofLiteralArgument(OTHERS_LITERAL, requirement));
            retargetCommandArguments.add(commandTargetArgumentIndex + 2, CommandArgument.ofRequiredArgument(PlayerCollection.class, OTHERS_LITERAL, false, requirement));
        } else {
            /* If the argument annotated with @CommandTarget is NOT annotated with @CommandSource, then the @CommandSource argument and @CommandTarget argument are already split. */
            retargetCommandArguments.set(commandTargetArgumentIndex, CommandArgument.ofLiteralArgument(OTHERS_LITERAL, requirement));
            retargetCommandArguments.add(commandTargetArgumentIndex + 1, CommandArgument.ofRequiredArgument(PlayerCollection.class, OTHERS_LITERAL, false, requirement));
        }

        return retargetCommandArguments;
    }

    @Override
    protected @NotNull Command<ServerCommandSource> makeCommandAction() {
        return withBaseCommandAction((commandContext) -> {

            LogUtil.debug("Execute retarget command (tree): initialing command source = {}, class = {}, method = {}"
                , commandContext.getSource().getName()
                , this.method.getDeclaringClass().getSimpleName()
                , this.method.getName());

            /* Invoke the command method. */
            List<Object> initialingParameterValues = makeMethodParameterValues(commandContext);
            LogUtil.debug("Initialing parameter values: {}", initialingParameterValues);

            /* Apply the command execution for each target. */
            PlayerCollection targets = (PlayerCollection) initialingParameterValues.get(this.othersParameterIndex);
            // NOTE: Remove the `others parameter` to match the signature of the original command method.
            initialingParameterValues.remove(othersParameterIndex);

            LogUtil.debug("Retrieved command targets: {}", targets.getValue().stream().map(PlayerHelper::getPlayerName).toList());
            int treeReturnValue = CommandHelper.Return.SUCCESS;
            for (ServerPlayerEntity target : targets.getValue()) {
                List<Object> executingParameterValues = new ArrayList<>(initialingParameterValues);
                // NOTE: Here you have to check whether the @CommandSource and @CommandTarget are in the same place, to handle the off-by-one error while injecting the parameter values.
                if (this.commandTargetParameterIndex == this.commandSourceParameterIndex) {
                    // Case: the argument annotated with @CommandTarget, is also annotated with @CommandSource.
                    executingParameterValues.set(this.commandTargetParameterIndex, target);
                } else {
                    // Case: the argument annotated with @CommandTarget, is not annotated with @CommandSource.
                    executingParameterValues.add(this.commandTargetParameterIndex, target);
                }
                LogUtil.debug("Executing parameter values: {}", executingParameterValues);
                LogUtil.debug("Execute retarget command (branch): initialing command source = {}, target = {}, class = {}, method = {}"
                    , commandContext.getSource().getName()
                    , PlayerHelper.getPlayerName(target)
                    , this.method.getDeclaringClass().getSimpleName()
                    , this.method.getName());

                try {
                    // If one of the execution if failed, then it's considered the whole return value is failed.
                    int branchCommandReturnValue = (int) this.method.invoke(null, executingParameterValues.toArray());
                    LogUtil.debug("Get the command return value of retarget command (branch): target = {}, returnValue = {}"
                        , PlayerHelper.getPlayerName(target)
                        , branchCommandReturnValue);

                    if (!CommandHelper.Return.isSuccess(branchCommandReturnValue)) {
                        treeReturnValue = CommandHelper.Return.FAILURE;
                    }

                } catch (Exception wrappedOrUnwrappedException) {
                    return CommandException.handleCommandExecutionException(commandContext, this.method, wrappedOrUnwrappedException);
                }
            }

            LogUtil.debug("Get the command return value of retarget command (tree): initialing command source = {}, returnValue = {}"
                , commandContext.getSource().getName()
                , treeReturnValue);
            return treeReturnValue;
        });
    }

}

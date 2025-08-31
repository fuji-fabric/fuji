package io.github.sakurawald.fuji.module.initializer.command_alias.structure;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.core.command.descriptor.CommandDescriptor;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import java.lang.reflect.Method;
import java.util.List;
import lombok.SneakyThrows;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public class AliasCommandDescriptor extends CommandDescriptor {

    final CommandNode<ServerCommandSource> redirectTargetCommandNode;

    public AliasCommandDescriptor(@NotNull List<CommandArgument> commandArguments, @NotNull CommandNode<ServerCommandSource> redirectTargetCommandNode) {
        super(getDummyMethod(), commandArguments);
        this.redirectTargetCommandNode = redirectTargetCommandNode;
    }

    @TestCase(action = "Test the redirect functionality.", targets = {
        "The redirect target command is a non-leaf command: `/invsee` -> `/view inv`",
        "The redirect target command is a leaf command: `/wb` -> `/workbench`"
    })
    @Override
    protected @NotNull ArgumentBuilder<ServerCommandSource, ?> terminalArgumentDecorator(@NotNull ArgumentBuilder<ServerCommandSource, ?> terminalArgumentBuilder) {
        return terminalArgumentBuilder
            // NOTE: The execute() handles the literal node to literal node case.
            .executes(this.redirectTargetCommandNode.getCommand())
            // NOTE: The redirect() only redirects to a command node with children.
            .redirect(this.redirectTargetCommandNode);
    }

    private static int dummyCommandActionMethod() {
        LogUtil.warn("The dummy method for redirect command descriptor is called. You should never see this.");
        return CommandHelper.Return.SUCCESS;
    }

    @SneakyThrows(NoSuchMethodException.class)
    private static Method getDummyMethod() {
        Method dummyMethod = AliasCommandDescriptor.class.getDeclaredMethod("dummyCommandActionMethod");
        dummyMethod.setAccessible(true);
        return dummyMethod;
    }

}

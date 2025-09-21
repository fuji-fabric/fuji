package mod.fuji.module.initializer.command_alias.structure;

import com.google.errorprone.annotations.Keep;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.descriptor.CommandDescriptor;
import mod.fuji.core.document.annotation.TestCase;
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

    @Keep
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

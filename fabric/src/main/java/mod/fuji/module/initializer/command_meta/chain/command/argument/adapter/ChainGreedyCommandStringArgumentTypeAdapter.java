package mod.fuji.module.initializer.command_meta.chain.command.argument.adapter;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import java.util.Set;
import mod.fuji.core.command.argument.adapter.impl.SeparatedGreedyCommandStringArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.module.initializer.command_meta.chain.command.argument.wrapper.ChainGreedyCommandString;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class ChainGreedyCommandStringArgumentTypeAdapter extends SeparatedGreedyCommandStringArgumentTypeAdapter {

    public ChainGreedyCommandStringArgumentTypeAdapter() {
        super(List.of("chain"));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(ChainGreedyCommandString.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("chain-greedy-command-string");
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        String string = StringArgumentType.getString(context, commandArgument.getArgumentName());
        return new ChainGreedyCommandString(string);
    }

}

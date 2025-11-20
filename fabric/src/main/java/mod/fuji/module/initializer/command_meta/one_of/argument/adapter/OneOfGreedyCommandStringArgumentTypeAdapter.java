package mod.fuji.module.initializer.command_meta.one_of.argument.adapter;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import java.util.Set;
import mod.fuji.core.command.argument.adapter.impl.SeparatedGreedyCommandStringArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.module.initializer.command_meta.one_of.argument.wrapper.OneOfGreedyCommandString;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class OneOfGreedyCommandStringArgumentTypeAdapter extends SeparatedGreedyCommandStringArgumentTypeAdapter {

    public OneOfGreedyCommandStringArgumentTypeAdapter() {
        super(List.of("one-of"));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(OneOfGreedyCommandString.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("one-of-greedy-command-string");
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        String string = StringArgumentType.getString(context, commandArgument.getArgumentName());
        return new OneOfGreedyCommandString(string);
    }
}

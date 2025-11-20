package mod.fuji.module.initializer.command_meta.OR.argument.adapter;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import java.util.Set;
import mod.fuji.core.command.argument.adapter.impl.SeparatedGreedyCommandStringArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.module.initializer.command_meta.OR.argument.wrapper.OrGreedyCommandString;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class OrGreedyCommandStringArgumentTypeAdapter extends SeparatedGreedyCommandStringArgumentTypeAdapter {

    public OrGreedyCommandStringArgumentTypeAdapter() {
        super(List.of("OR"));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(OrGreedyCommandString.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("or-greedy-command-string");
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        String string = StringArgumentType.getString(context, commandArgument.getArgumentName());
        return new OrGreedyCommandString(string);
    }
}

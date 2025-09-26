package mod.fuji.module.initializer.command_meta.AND.command.argument.adapter;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import java.util.Set;
import mod.fuji.core.command.argument.adapter.impl.SeparatedGreedyCommandStringArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.module.initializer.command_meta.AND.command.argument.wrapper.AndGreedyCommandString;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public class AndGreedyCommandStringArgumentTypeAdapter extends SeparatedGreedyCommandStringArgumentTypeAdapter {

    public AndGreedyCommandStringArgumentTypeAdapter() {
        super(Set.of("AND"));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(AndGreedyCommandString.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("and-greedy-command-string");
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        String string = StringArgumentType.getString(context, commandArgument.getArgumentName());
        return new AndGreedyCommandString(string);
    }
}

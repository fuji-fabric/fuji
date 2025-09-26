package mod.fuji.module.initializer.command_meta.IF.argument.adapter;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import mod.fuji.core.command.argument.adapter.impl.SeparatedGreedyCommandStringArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.module.initializer.command_meta.IF.argument.wrapper.IfGreedyCommandString;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public class IfGreedyCommandStringArgumentTypeAdapter extends SeparatedGreedyCommandStringArgumentTypeAdapter {

    public IfGreedyCommandStringArgumentTypeAdapter() {
        super(List.of("ELSE", "THEN"));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(IfGreedyCommandString.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("if-greedy-command-string");
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        String string = StringArgumentType.getString(context, commandArgument.getArgumentName());
        return new IfGreedyCommandString(string);
    }
}

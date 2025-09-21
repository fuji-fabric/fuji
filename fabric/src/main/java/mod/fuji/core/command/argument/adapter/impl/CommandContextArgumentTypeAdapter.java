package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class CommandContextArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        throw new UnsupportedOperationException("You should add `@CommandSource` annotation before the CommandContext<ServerCommandSource> !");
    }

    @Override
    public Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        return context;
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(CommandContext.class);
    }

    @Override
    public List<String> getTypeNames() {
        return Collections.emptyList();
    }
}

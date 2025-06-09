package io.github.sakurawald.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collections;
import java.util.List;

public class CommandContextArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        throw new UnsupportedOperationException("You should add `@CommandSource` annotation before the CommandContext<ServerCommandSource> !");
    }

    @Override
    public Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return context;
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(CommandContext.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return Collections.emptyList();
    }
}

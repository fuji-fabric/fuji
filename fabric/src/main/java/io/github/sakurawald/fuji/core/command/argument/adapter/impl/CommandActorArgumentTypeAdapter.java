package io.github.sakurawald.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.core.command.structure.CommandActor;
import java.util.List;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public class CommandActorArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(CommandActor.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of();
    }

    @Override
    protected ArgumentType<?> makeArgumentType() {
        throw new UnsupportedOperationException("The CommandActor parameter should be injected directly.");
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        return new CommandActor(context);
    }
}

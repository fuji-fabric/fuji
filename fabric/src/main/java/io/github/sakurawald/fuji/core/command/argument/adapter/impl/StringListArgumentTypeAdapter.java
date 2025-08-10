package io.github.sakurawald.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.StringList;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class StringListArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        String string = StringArgumentType.getString(context, commandArgument.getArgumentName());
        List<String> stringList = Arrays.stream(string.split("\\|")).toList();
        return new StringList(stringList);
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(StringList.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("string-list");
    }
}

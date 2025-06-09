package io.github.sakurawald.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import io.github.sakurawald.core.command.argument.wrapper.impl.Word;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class WordArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.word();
    }

    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return new Word(StringArgumentType.getString(context, argument.getArgumentName()));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(Word.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("word");
    }
}

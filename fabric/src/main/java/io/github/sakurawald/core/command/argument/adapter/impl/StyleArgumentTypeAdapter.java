package io.github.sakurawald.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import io.github.sakurawald.core.command.processor.CommandAnnotationProcessor;
import net.minecraft.command.argument.StyleArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;

import java.util.List;

public class StyleArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StyleArgumentType.style(CommandAnnotationProcessor.getRegistryAccess());
    }

    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return StyleArgumentType.getStyle(context, argument.getArgumentName());
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(Style.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("style");
    }
}

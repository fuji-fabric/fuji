package io.github.sakurawald.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import io.github.sakurawald.core.command.processor.CommandAnnotationProcessor;
import net.minecraft.command.argument.ItemPredicateArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class ItemPredicateArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return ItemPredicateArgumentType.itemPredicate(CommandAnnotationProcessor.getRegistryAccess());
    }

    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return ItemPredicateArgumentType.getItemStackPredicate(context, argument.getArgumentName());
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(ItemPredicateArgumentType.ItemStackPredicateArgument.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("item-predicate");
    }
}

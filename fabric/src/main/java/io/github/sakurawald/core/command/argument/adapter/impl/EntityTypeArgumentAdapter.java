package io.github.sakurawald.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import io.github.sakurawald.core.command.argument.wrapper.impl.NotSupportedType;
import io.github.sakurawald.core.command.processor.CommandAnnotationProcessor;
import lombok.SneakyThrows;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class EntityTypeArgumentAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return RegistryEntryReferenceArgumentType.registryEntry(CommandAnnotationProcessor.getRegistryAccess(), RegistryKeys.ENTITY_TYPE);
    }

    @SneakyThrows
    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return RegistryEntryReferenceArgumentType.getRegistryEntry(context, argument.getArgumentName(), RegistryKeys.ENTITY_TYPE);
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(NotSupportedType.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("entity-type");
    }
}

package io.github.sakurawald.fuji.module.initializer.command_cooldown.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.Argument;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.service.NamedCooldownService;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.structure.NamedCooldownDescriptor;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class NamedCooldownDescriptorArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        String id = StringArgumentType.getString(context, argument.getArgumentName());
        return NamedCooldownService
            .findNamedCooldownDescriptor(id)
            .orElseThrow(() -> {
                TextHelper.sendTextByKey(context.getSource(), "command_cooldown.not_found", id);
                return new AbortCommandExecutionException();
            });
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable(() -> NamedCooldownService.getNamedCooldownDescriptors().keySet()));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(NamedCooldownDescriptor.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("named-command-cooldown");
    }
}

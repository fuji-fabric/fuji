package io.github.sakurawald.fuji.module.initializer.jail.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.module.initializer.jail.service.JailService;
import io.github.sakurawald.fuji.module.initializer.jail.structure.JailDescriptor;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public class JailDescriptorArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        String jailId = StringArgumentType.getString(context, commandArgument.getArgumentName());
        Optional<JailDescriptor> jailDescriptor = JailService.findJailDescriptor(jailId);

        return jailDescriptor.orElseThrow(() -> {
            TextHelper.sendTextByKey(context.getSource(), "jail.not_found", jailId);
            return new AbortCommandExecutionException();
        });
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(JailDescriptor.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("jail-id");
    }

    @Override
    @NotNull
    protected RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable(JailService::getJailIds));
    }
}

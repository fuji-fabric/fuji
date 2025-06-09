package io.github.sakurawald.module.initializer.echo.send_custom.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import io.github.sakurawald.module.initializer.echo.send_custom.SendCustomInitializer;
import io.github.sakurawald.module.initializer.echo.send_custom.command.argument.wrapper.CustomTextName;
import lombok.Cleanup;
import lombok.SneakyThrows;
import net.minecraft.server.command.ServerCommandSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class CustomTextNameArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return new CustomTextName(StringArgumentType.getString(context, argument.getArgumentName()));
    }

    @SneakyThrows
    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName).suggests(
            CommandHelper.Suggestion.iterable(() -> {
                    try {
                        @Cleanup Stream<Path> list = Files.list(SendCustomInitializer.CUSTOM_TEXT_DIR_PATH);
                        return list
                            .filter(it -> it.toFile().isFile())
                            .map(Path::getFileName)
                            .toList();
                    } catch (IOException e) {
                        LogUtil.error("failed to list suggestions for custom text names", e);
                    }

                    return Collections.emptyList();
                }
            ));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(CustomTextName.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("custom-text-name");
    }
}

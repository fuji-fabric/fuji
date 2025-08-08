package io.github.sakurawald.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.Argument;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.Duration;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.core.service.date_parser.DateParser;
import java.util.List;
import net.minecraft.server.command.ServerCommandSource;

public class DurationArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        String durationString = StringArgumentType.getString(context, argument.getArgumentName());

        try {
            DateParser.parseIntoSeconds(durationString);
        } catch (IllegalArgumentException e) {
            TextHelper.sendTextByKey(context.getSource(), "duration.invalid", durationString);
            throw new AbortCommandExecutionException();
        }
        return new Duration(durationString);
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(Duration.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("duration");
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable(() -> List.of("30m", "12h", "1d", "1w", "1M")));
    }
}

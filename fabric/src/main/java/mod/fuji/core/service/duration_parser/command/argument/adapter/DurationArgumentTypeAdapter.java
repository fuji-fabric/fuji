package mod.fuji.core.service.duration_parser.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.service.duration_parser.command.argument.wrapper.Duration;
import mod.fuji.core.command.exception.AbortCommandExecutionException;
import mod.fuji.core.service.duration_parser.DurationParser;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class DurationArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        String durationString = StringArgumentType.getString(context, commandArgument.getArgumentName());

        return DurationParser
            .parseIntoSeconds(durationString)
            .map(it -> new Duration(durationString))
            .orElseThrow(() -> {
                TextHelper.sendTextByKey(context.getSource(), "duration.invalid", durationString);
                return new AbortCommandExecutionException();
            });
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(Duration.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("duration");
    }

    @Override
    @NotNull
    protected RequiredArgumentBuilder<CommandSourceStack, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable(() -> List.of("30m", "12h", "1d", "3d", "1w", "1M")));
    }
}

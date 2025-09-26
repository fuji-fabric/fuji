package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.argument.wrapper.impl.GreedyCommandString;
import mod.fuji.core.document.annotation.TestCase;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class GreedyCommandStringArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(GreedyCommandString.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("greedy-command-string");
    }

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.greedyString();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        String string = StringArgumentType.getString(context, commandArgument.getArgumentName());
        return new GreedyCommandString(string);
    }

    @TestCase(action = "Test the functionality for recursive suggestions builder.", targets = {
        "Issue: `/run as console send-broadcast <rb>I am %player:name%`",
        "Issue: `/run as player @s run as console run as fake-op %player:name% say I am %player:name%`",
        "Issue: `/run as console command-attachment attach-entity-one @e[type=...`",
        "Issue: `/NOT NOT NOT run as console delay 3 foreach when-online %player:name% send-broadcast You are %player:name%`"
    })
    @Override
    protected @NotNull RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests((context, builder) -> {
                /* Skip for non-player command source. */
                if (context.getSource().getPlayer() == null) {
                    return builder.buildFuture();
                }
                ServerPlayerEntity player = context.getSource().getPlayer();

                /* Make the recursive suggestions builder. */
                String remainingString = builder.getRemaining();
//                LogUtil.warn("remaining = {}", remainingString);
//                LogUtil.warn("remaining length = {}", remainingString.length());
//
//                LogUtil.warn("builder.getInput() = {}", builder.getInput());
//                LogUtil.warn("builder.getStart() = {}", builder.getStart());

                @NotNull Suggestions remainingSuggestions = CommandHelper.Suggestion.listSuggestions(player, remainingString);
                int offsetBuilderStart = builder.getStart() + remainingSuggestions.getRange().getStart();
                SuggestionsBuilder offsetBuilder = builder.createOffset(offsetBuilderStart);
//                LogUtil.warn("offsetBuilder.getInput() = {}", offsetBuilder.getInput());
//                LogUtil.warn("offsetBuilder.getStart() = {}", offsetBuilder.getStart());

                remainingSuggestions.getList().forEach(it -> {
//                    LogUtil.warn("suggestion = {}", it);
                    offsetBuilder.suggest(it.getText());
                });

                return offsetBuilder.buildFuture();
            });
    }
}

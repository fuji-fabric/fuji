package mod.fuji.module.initializer.rank.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.exception.AbortCommandExecutionException;
import mod.fuji.module.initializer.rank.service.RankService;
import mod.fuji.module.initializer.rank.structure.RankNode;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class RankNodeArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        String rankId = StringArgumentType.getString(context, commandArgument.getArgumentName());
        return RankService
            .findRankNode(rankId)
            .orElseThrow(() -> {
                TextHelper.sendTextByKey(context.getSource(), "rank.rank_node.not_found", rankId);
                return new AbortCommandExecutionException();
            });
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(RankNode.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("rank-id");
    }

    @Override
    @NotNull
    protected RequiredArgumentBuilder<CommandSourceStack, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable(RankService::getAllRankIds));
    }
}

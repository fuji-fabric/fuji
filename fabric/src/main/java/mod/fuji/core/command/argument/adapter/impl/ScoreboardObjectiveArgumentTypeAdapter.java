package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import lombok.SneakyThrows;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.world.scores.Objective;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ScoreboardObjectiveArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return ObjectiveArgument.objective();
    }

    @SneakyThrows(CommandSyntaxException.class)
    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        return ObjectiveArgument.getObjective(context, commandArgument.getArgumentName());
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(Objective.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("scoreboard-objective");
    }
}

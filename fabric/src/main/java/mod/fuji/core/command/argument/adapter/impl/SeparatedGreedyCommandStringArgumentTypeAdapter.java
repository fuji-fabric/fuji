package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import mod.fuji.core.command.argument.structure.CommandArgument;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public abstract class SeparatedGreedyCommandStringArgumentTypeAdapter extends GreedyCommandStringArgumentTypeAdapter {

    @Override
    public abstract List<Class<?>> getTypeClasses();

    @Override
    public abstract List<String> getTypeNames();

    @Override
    protected abstract Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument);

    public SeparatedGreedyCommandStringArgumentTypeAdapter(@NotNull List<String> greedyStringSeparatorLiterals) {
        super(greedyStringSeparatorLiterals);
    }

}

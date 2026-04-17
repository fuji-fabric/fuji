package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import lombok.SneakyThrows;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.argument.wrapper.impl.Dimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import org.jetbrains.annotations.NotNull;

public class DimensionArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return DimensionArgument.dimension();
    }

    /**
     * 1. The DimensionArgumentType.dimension() will not suggest the new registered dimensions, or un-registered dimensions.
     * 2. The dimension registry is synced when the client joins the server, and it's fixed.
     **/
    @Override
    @NotNull
    protected RequiredArgumentBuilder<CommandSourceStack, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(
                (ctx, builder) -> {
                    WorldHelper.getWorlds().forEach(it -> builder.suggest(RegistryHelper.getIdAsString(it)));
                    return builder.buildFuture();
                }
            );
    }

    @SneakyThrows(CommandSyntaxException.class)
    @Override
    public Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        return new Dimension(DimensionArgument.getDimension(context, commandArgument.getArgumentName()));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(Dimension.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("dimension", "world");
    }

    @Override
    public boolean isVanillaMinecraftArgumentType() {
        return true;
    }
}

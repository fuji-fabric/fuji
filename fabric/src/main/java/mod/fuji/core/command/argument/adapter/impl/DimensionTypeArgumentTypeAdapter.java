package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.representation.IdentifierArgumentTypeIR;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.argument.wrapper.impl.DimensionType;
import net.minecraft.core.registries.Registries;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class DimensionTypeArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return IdentifierArgumentTypeIR.makeArgumentType();
    }

    @Override
    public Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        var nativeValue = IdentifierArgumentTypeIR.makeArgumentValue(context,commandArgument);
        return new DimensionType(nativeValue.toString());
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(DimensionType.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("dimension-type", "world-type");
    }

    @Override
    @NotNull
    protected RequiredArgumentBuilder<CommandSourceStack, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName).suggests(CommandHelper.Suggestion.identifiers(Registries.DIMENSION_TYPE));
    }

    @Override
    public boolean isVanillaMinecraftArgumentType() {
        return true;
    }
}

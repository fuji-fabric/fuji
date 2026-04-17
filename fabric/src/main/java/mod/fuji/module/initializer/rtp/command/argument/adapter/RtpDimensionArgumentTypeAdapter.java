package mod.fuji.module.initializer.rtp.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.representation.IdentifierArgumentTypeIR;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.structure.IdentifierIR;
import mod.fuji.module.initializer.rtp.RtpInitializer;
import mod.fuji.module.initializer.rtp.command.argument.wrapper.RtpDimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

public class RtpDimensionArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(RtpDimension.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("rtp-dimension");
    }

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return IdentifierArgumentTypeIR.makeArgumentType();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        IdentifierIR identifierIR = IdentifierIR.of(IdentifierArgumentTypeIR.makeArgumentValue(context, commandArgument));
        String dimensionId = RegistryHelper.getIdAsString(identifierIR);
        ServerLevel world = WorldHelper.getWorldOrThrow(dimensionId);
        return new RtpDimension(world);
    }

    @Override
    protected @NotNull RequiredArgumentBuilder<CommandSourceStack, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(
                (ctx, builder) -> {
                    WorldHelper
                        .getWorlds()
                        .stream()
                        .filter(it -> RtpInitializer.getRandomTeleportSettings(it).isPresent())
                        .forEach(it -> builder.suggest(RegistryHelper.getIdAsString(it)));
                    return builder.buildFuture();
                }
            );
    }
}

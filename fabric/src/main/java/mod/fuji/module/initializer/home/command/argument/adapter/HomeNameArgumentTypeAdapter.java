package mod.fuji.module.initializer.home.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.module.initializer.home.command.argument.wrapper.HomeName;
import mod.fuji.module.initializer.home.service.HomeService;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class HomeNameArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    public Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        String homeName = StringArgumentType.getString(context, commandArgument.getArgumentName());
        return new HomeName(homeName);
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(HomeName.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("home-name");
    }

    @Override
    @NotNull
    protected RequiredArgumentBuilder<CommandSourceStack, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests((context, builder) -> {
                ServerPlayer player = context.getSource().getPlayer();
                if (player == null) return builder.buildFuture();

                String playerName = PlayerHelper.getPlayerName(player);
                HomeService
                    .withHomeMap(playerName)
                    .keySet()
                    .forEach(builder::suggest);
                return builder.buildFuture();
            }
        );
    }
}

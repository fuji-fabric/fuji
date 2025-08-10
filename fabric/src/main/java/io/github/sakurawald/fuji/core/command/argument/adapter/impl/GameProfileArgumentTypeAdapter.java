package io.github.sakurawald.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.Argument;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GameProfileCollection;
import lombok.SneakyThrows;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class GameProfileArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return GameProfileArgumentType.gameProfile();
    }

    @SneakyThrows(CommandSyntaxException.class)
    @Override
    public Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull Argument argument) {
        return new GameProfileCollection(GameProfileArgumentType.getProfileArgument(context, argument.getArgumentName()));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(GameProfileCollection.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("game-profile", "profile");
    }

    @Override
    public boolean isVanillaMinecraftArgumentType() {
        return true;
    }
}

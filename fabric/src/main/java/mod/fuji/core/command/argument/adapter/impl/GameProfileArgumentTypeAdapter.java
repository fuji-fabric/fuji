package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.argument.wrapper.impl.GameProfileCollection;
import java.util.List;
import lombok.SneakyThrows;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class GameProfileArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return GameProfileArgument.gameProfile();
    }

    @SneakyThrows(CommandSyntaxException.class)
    @Override
    public Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        var gameProfiles = GameProfileArgument.getGameProfiles(context, commandArgument.getArgumentName());
        return new GameProfileCollection(gameProfiles);
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(GameProfileCollection.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("game-profile", "profile");
    }

    @Override
    public boolean isVanillaMinecraftArgumentType() {
        return true;
    }
}

package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import lombok.SneakyThrows;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class PlayerArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    public ArgumentType<?> makeArgumentType() {
        return EntityArgument.player();
    }

    @SneakyThrows(CommandSyntaxException.class)
    @Override
    public Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        if (commandArgument.isCommandSource()) {
            return context.getSource().getPlayer();
        }

        return EntityArgument.getPlayer(context, commandArgument.getArgumentName());
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(ServerPlayer.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("player");
    }

    @Override
    public boolean verifyCommandSource(@NotNull CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            TextHelper.sendTextByKey(context.getSource(), "command.player_only");
            return false;
        }

        return true;
    }
}

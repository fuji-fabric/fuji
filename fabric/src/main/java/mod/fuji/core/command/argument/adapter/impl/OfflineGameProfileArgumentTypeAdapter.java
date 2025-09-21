package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.argument.wrapper.impl.OfflineGameProfile;
import mod.fuji.core.command.exception.AbortCommandExecutionException;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public class OfflineGameProfileArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    public ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    @NotNull
    protected RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName).suggests(CommandHelper.Suggestion.iterable(PlayerHelper.Cache::getOfflinePlayerNames));
    }

    @Override
    public Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        String offlinePlayerName = StringArgumentType.getString(context, commandArgument.getArgumentName());

        Optional<GameProfile> offlineGameProfile = PlayerHelper.Cache.getOfflineGameProfileByName(offlinePlayerName);
        return offlineGameProfile
            .map(OfflineGameProfile::new)
            .orElseThrow(() -> {
                TextHelper.sendTextByKey(context.getSource(), "player.unknown_player");
                return new AbortCommandExecutionException();
            });
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(OfflineGameProfile.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("offline-game-profile");
    }
}

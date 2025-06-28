package io.github.sakurawald.fuji.module.mixin.gameplay.carpet.fake_player_manager;

import carpet.commands.PlayerCommand;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.gameplay.carpet.fake_player_manager.FakePlayerManagerInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("DataFlowIssue")
@Mixin(PlayerCommand.class)
public abstract class PlayerCommandMixin {

    @Unique
    private static @NotNull String transformFakePlayerName(@NotNull String fakePlayerName) {
        return FakePlayerManagerInitializer.config.model().transform_name
            .replace("%name%", fakePlayerName);
    }

    @Redirect(method = "cantSpawn", at = @At(
        value = "INVOKE",
        target = "Lcom/mojang/brigadier/arguments/StringArgumentType;getString(Lcom/mojang/brigadier/context/CommandContext;Ljava/lang/String;)Ljava/lang/String;"
    ), remap = false)
    private static @NotNull String $canSpawn(final @NotNull CommandContext<?> context, final String name) {
        return transformFakePlayerName(StringArgumentType.getString(context, name));
    }

    @Redirect(method = "spawn", at = @At(
        value = "INVOKE",
        target = "Lcom/mojang/brigadier/arguments/StringArgumentType;getString(Lcom/mojang/brigadier/context/CommandContext;Ljava/lang/String;)Ljava/lang/String;"
    ), remap = false)
    private static @NotNull String $spawn(final @NotNull CommandContext<?> context, final String name) {
        return transformFakePlayerName(StringArgumentType.getString(context, name));
    }

    @Inject(method = "spawn", at = @At("HEAD"), remap = false, cancellable = true)
    private static void $spawn_head(@NotNull CommandContext<ServerCommandSource> context, @NotNull CallbackInfoReturnable<Integer> cir) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return;

        /* check: caps */
        if (!FakePlayerManagerInitializer.canSpawnFakePlayer(player)) {
            TextHelper.sendMessageByKey(player, "fake_player_manager.spawn.limit_exceed");
            cir.setReturnValue(CommandHelper.Return.FAIL);
        }
    }

    @Inject(method = "spawn", at = @At("TAIL"), remap = false)
    private static void $spawn_tail(@NotNull CommandContext<ServerCommandSource> context, CallbackInfoReturnable<Integer> cir) {
        /* transform fake-player name */
        String playerArg = StringArgumentType.getString(context, "player");
        playerArg = transformFakePlayerName(playerArg);

        /* track it */
        ServerPlayerEntity player = context.getSource().getPlayer();
        FakePlayerManagerInitializer.addMyFakePlayer(player, playerArg);
        FakePlayerManagerInitializer.renewMyFakePlayers(player);
    }

    @Inject(method = "cantManipulate", at = @At("HEAD"), remap = false, cancellable = true)
    private static void $cantManipulate(@NotNull CommandContext<ServerCommandSource> context, @NotNull CallbackInfoReturnable<Boolean> cir) {
        String playerArg = StringArgumentType.getString(context, "player");

        if (!FakePlayerManagerInitializer.canManipulateFakePlayer(context, playerArg)) {
            TextHelper.sendMessageByKey(context.getSource(), "fake_player_manager.manipulate.forbidden");
            cir.setReturnValue(true);
        }
    }
}

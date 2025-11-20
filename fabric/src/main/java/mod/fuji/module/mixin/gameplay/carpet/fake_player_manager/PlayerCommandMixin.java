package mod.fuji.module.mixin.gameplay.carpet.fake_player_manager;

import carpet.commands.PlayerCommand;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.module.initializer.gameplay.carpet.fake_player_manager.service.FakePlayerManagerService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("DataFlowIssue")
@Mixin(PlayerCommand.class)
public abstract class PlayerCommandMixin {

    @WrapOperation(method = "cantSpawn", at = @At(
        value = "INVOKE",
        target = "Lcom/mojang/brigadier/arguments/StringArgumentType;getString(Lcom/mojang/brigadier/context/CommandContext;Ljava/lang/String;)Ljava/lang/String;"
    ), remap = false)
    private static @NotNull String modifyFakePlayerNameForCantSpawnMethod(CommandContext<?> context, String name, Operation<String> original) {
        return FakePlayerManagerService.getTransformedFakePlayerName(StringArgumentType.getString(context, name));
    }

    @WrapOperation(method = "spawn", at = @At(
        value = "INVOKE",
        target = "Lcom/mojang/brigadier/arguments/StringArgumentType;getString(Lcom/mojang/brigadier/context/CommandContext;Ljava/lang/String;)Ljava/lang/String;"
    ), remap = false)
    private static @NotNull String modifyFakePlayerNameForSpawnMethod(CommandContext<?> context, String name, Operation<String> original) {
        return FakePlayerManagerService.getTransformedFakePlayerName(StringArgumentType.getString(context, name));
    }

    @Inject(method = "spawn", at = @At("HEAD"), remap = false, cancellable = true)
    private static void checkFakePlayerCapsOnSpawnCommand(@NotNull CommandContext<CommandSourceStack> context, @NotNull CallbackInfoReturnable<Integer> cir) {
        if (CommandHelper.Source.isExecutedByConsole(context)) {
            return;
        }

        ServerPlayer player = context.getSource().getPlayer();
        if (!FakePlayerManagerService.canSpawnMoreFakePlayers(player)) {
            TextHelper.sendTextByKey(player, "fake_player_manager.spawn.limit_exceed");
            cir.setReturnValue(CommandHelper.Return.FAILURE);
        }
    }

    @Inject(method = "spawn", at = @At("TAIL"), remap = false)
    private static void trackSpawnedFakePlayerOnSpawnCommand(@NotNull CommandContext<CommandSourceStack> context, CallbackInfoReturnable<Integer> cir) {
        if (CommandHelper.Source.isExecutedByConsole(context)) {
            return;
        }

        /* Transform the fake player name to get the proper fake player name. */
        ServerPlayer player = context.getSource().getPlayer();
        String fakePlayerName = StringArgumentType.getString(context, "player");
        fakePlayerName = FakePlayerManagerService.getTransformedFakePlayerName(fakePlayerName);

        /* Track it. */
        FakePlayerManagerService.addMyFakePlayer(player, fakePlayerName);
        FakePlayerManagerService.renewMyFakePlayers(player);
    }

    @Inject(method = "cantManipulate", at = @At("HEAD"), remap = false, cancellable = true)
    private static void validateAuthorityOnPlayerCommands(@NotNull CommandContext<CommandSourceStack> context, @NotNull CallbackInfoReturnable<Boolean> cir) {
        String fakePlayerName = StringArgumentType.getString(context, "player");

        if (!FakePlayerManagerService.canManipulateFakePlayer(context, fakePlayerName)) {
            TextHelper.sendTextByKey(context.getSource(), "fake_player_manager.manipulate.forbidden");
            cir.setReturnValue(true);
        }
    }
}

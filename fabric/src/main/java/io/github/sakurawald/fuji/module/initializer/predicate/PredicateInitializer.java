package io.github.sakurawald.fuji.module.initializer.predicate;

import io.github.sakurawald.fuji.core.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PermissionHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.Dimension;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.core.structure.descriptor.PermissionDescriptor;
import net.minecraft.command.argument.ItemPredicateArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

import java.util.Optional;

@Document("""
    Provides `predicate` commands, suffix with `?`.
    """)
@CommandRequirement(level = 4)
public class PredicateInitializer extends ModuleInitializer {

    @Document("Predicate to test if the player has the string-perm?")
    @CommandNode("has-perm?")
    private static int hasPerm(@CommandSource ServerCommandSource source, ServerPlayerEntity player, GreedyString stringPermission) {
        boolean value = PermissionHelper.hasPermission(player.getUuid(), new PermissionDescriptor(true, stringPermission.getValue(), null));
        return CommandHelper.Return.outputBoolean(source, value);
    }

    @Document("Predicate to test if the player has the level-perm?")
    @CommandNode("has-level?")
    private static int hasLevel(@CommandSource ServerCommandSource source, ServerPlayerEntity player, int levelPermission) {
        boolean value = player.hasPermissionLevel(levelPermission);
        return CommandHelper.Return.outputBoolean(source, value);
    }

    @Document("Predicate if online players >= n.")
    @CommandNode("has-players?")
    private static int hasPlayers(@CommandSource ServerCommandSource source, Optional<Integer> n) {
        int $n = n.orElse(0);
        boolean value = ServerHelper.getPlayers().size() >= $n;
        return CommandHelper.Return.outputBoolean(source, value);
    }

    @CommandNode("is-op?")
    private static int isOp(@CommandSource ServerCommandSource source, ServerPlayerEntity player) {
        boolean value = ServerHelper.getPlayerManager().isOperator(player.getGameProfile());
        return CommandHelper.Return.outputBoolean(source, value);
    }

    @CommandNode("is-holding?")
    private static int isHolding(@CommandSource ServerCommandSource source, ServerPlayerEntity player, ItemPredicateArgumentType.ItemStackPredicateArgument itemPredicate) {
        boolean value = player.isHolding(itemPredicate);
        return CommandHelper.Return.outputBoolean(source, value);
    }

    @CommandNode("has-exp?")
    private static int hasExp(@CommandSource ServerCommandSource source, ServerPlayerEntity player, int exp) {
        boolean value = player.totalExperience >= exp;
        return CommandHelper.Return.outputBoolean(source, value);
    }

    @CommandNode("has-exp-level?")
    private static int hasExpLevel(@CommandSource ServerCommandSource source, ServerPlayerEntity player, int expLevel) {
        boolean value = player.experienceLevel >= expLevel;
        return CommandHelper.Return.outputBoolean(source, value);
    }

    @CommandNode("is-in-world?")
    private static int isInWorld(@CommandSource ServerCommandSource source, ServerPlayerEntity player, Dimension dimension) {
        boolean value = EntityHelper.getServerWorld(player).equals(dimension.getValue());
        return CommandHelper.Return.outputBoolean(source, value);
    }

    @CommandNode("is-in-gamemode?")
    private static int isInGameMode(@CommandSource ServerCommandSource source, ServerPlayerEntity player, GameMode gameMode) {
        boolean value = player.interactionManager.getGameMode().equals(gameMode);
        return CommandHelper.Return.outputBoolean(source, value);
    }
}

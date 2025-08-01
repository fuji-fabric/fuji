package io.github.sakurawald.fuji.module.initializer.predicate;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.Dimension;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import net.minecraft.command.argument.ItemPredicateArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

import java.util.Optional;

@Document(id = 1751826497994L, value = """
    Provides `predicate` commands, which suffix with `?`.
    """)

@ColorBox(id = 1751978705157L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ How it works?
    A `predicate command` will returns `SUCCESS` if `true`, and `FAILED` if `false`.
    It relies on the `return value of command` in vanilla Minecraft.
    See https://minecraft.fandom.com/wiki/Commands/return

    Actually, you can also use the `predicate command` with `command block`.
    """)
@ColorBox(id = 1751978789271L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Test a condition, and then run a command if success.
    Issue: `/run as player Alice chain has-perm? %player:name% 4 chain say value is true`
    """)


@CommandRequirement(level = 4)
public class PredicateInitializer extends ModuleInitializer {

    @CommandNode("has-perm?")
    private static int $hasPerm(@CommandSource ServerCommandSource source, ServerPlayerEntity player, GreedyString stringPermission) {
        boolean value = LuckpermsHelper.hasPermission(player.getUuid(), new PermissionDescriptor(true, stringPermission.getValue(), 0));
        return CommandHelper.Return.returnBoolean(source, value);
    }

    @Document(id = 1751826502598L, value = "Predicate to test if the player has the level-perm?")
    @CommandNode("has-level?")
    private static int $hasLevel(@CommandSource ServerCommandSource source, ServerPlayerEntity player, int levelPermission) {
        boolean value = player.hasPermissionLevel(levelPermission);
        return CommandHelper.Return.returnBoolean(source, value);
    }

    @Document(id = 1751826504480L, value = "Predicate if online players >= n.")
    @CommandNode("has-players?")
    private static int $hasPlayers(@CommandSource ServerCommandSource source, Optional<Integer> n) {
        int $n = n.orElse(0);
        boolean value = PlayerHelper.Lookup.getOnlinePlayers().size() >= $n;
        return CommandHelper.Return.returnBoolean(source, value);
    }

    @CommandNode("is-op?")
    private static int $isOp(@CommandSource ServerCommandSource source, ServerPlayerEntity player) {
        boolean value = PlayerHelper.isOperator(player);
        return CommandHelper.Return.returnBoolean(source, value);
    }

    @CommandNode("is-holding?")
    private static int $isHolding(@CommandSource ServerCommandSource source, ServerPlayerEntity player, ItemPredicateArgumentType.ItemStackPredicateArgument itemPredicate) {
        boolean value = player.isHolding(itemPredicate);
        return CommandHelper.Return.returnBoolean(source, value);
    }

    @CommandNode("has-exp?")
    private static int $hasExp(@CommandSource ServerCommandSource source, ServerPlayerEntity player, int exp) {
        boolean value = player.totalExperience >= exp;
        return CommandHelper.Return.returnBoolean(source, value);
    }

    @CommandNode("has-exp-level?")
    private static int $hasExpLevel(@CommandSource ServerCommandSource source, ServerPlayerEntity player, int expLevel) {
        boolean value = player.experienceLevel >= expLevel;
        return CommandHelper.Return.returnBoolean(source, value);
    }

    @CommandNode("is-in-world?")
    private static int $isInWorld(@CommandSource ServerCommandSource source, ServerPlayerEntity player, Dimension dimension) {
        boolean value = EntityHelper.getServerWorld(player).equals(dimension.getValue());
        return CommandHelper.Return.returnBoolean(source, value);
    }

    @CommandNode("is-in-gamemode?")
    private static int $isInGameMode(@CommandSource ServerCommandSource source, ServerPlayerEntity player, GameMode gameMode) {
        boolean value = player.interactionManager.getGameMode().equals(gameMode);
        return CommandHelper.Return.returnBoolean(source, value);
    }
}

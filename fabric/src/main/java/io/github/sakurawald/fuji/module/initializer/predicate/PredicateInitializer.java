package io.github.sakurawald.fuji.module.initializer.predicate;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.InventoryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
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
import java.util.Objects;
import java.util.function.BiPredicate;
import net.minecraft.command.argument.ItemPredicateArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.Optional;

@Document(id = 1751826497994L, value = """
    Provides `predicate` commands, which suffix with `?`.
    """)

@ColorBox(id = 1751978705157L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ How it works?
    A `predicate command` will return an `integer value` to represent the `SUCCESS` or `FAILED` test result.
    It relies on the `return value of command` in vanilla Minecraft.
    See https://minecraft.fandom.com/wiki/Commands/return

    Actually, you can also use the `predicate command` with `command block`.
    """)
@ColorBox(id = 1751978789271L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Test a condition, and then run a command if success.
    Issue: `/run as player Alice chain has-perm? %player:name% 4 chain say value is true`
    """)
@ColorBox(id = 1754448424606L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Test if a player is holding an apple
    Issue: `/command-debug is-holding? Steve minecraft:apple`

    ◉ Test if a player has specified string permission
    Issue: `/command-debug has-perm? Steve fuji.permission.back`

    ◉ Test if a player's health >= 10
    Issue: `/command-debug <=? Steve 10 %player:health%`

    ◉ Test if a player killed more than 100 zombies.
    Issue: `/command-debug <=? Steve 100 %player:statistic_raw minecraft:deaths%`

    ◉ Leverage the `vanilla Minecraft statistics`
    See: https://minecraft.fandom.com/wiki/Statistics

    ◉ More examples
    You can see more examples in `command_meta.IF` module and `command_meta.chain` module.
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

    @CommandNode("has-item?")
    private static int $hasItem(@CommandSource ServerCommandSource source, ServerPlayerEntity player, ItemPredicateArgumentType.ItemStackPredicateArgument itemPredicate, int count) {
        int matchCount = 0;
        for (ItemStack inventoryStack : InventoryHelper.getInventoryStacks(player)) {
            boolean test = itemPredicate.test(inventoryStack);
            if (test) {
                matchCount += inventoryStack.getCount();
            }
        }

        boolean success = matchCount >= count;
        return CommandHelper.Return.returnBoolean(source, success);
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

    @CommandNode("=?")
    private static int $equalsNumber(@CommandSource ServerCommandSource source, ServerPlayerEntity player, double value, GreedyString numericValueProvider) {
        return compareNumericValue(source, player, value, numericValueProvider, Objects::equals);
    }

    @CommandNode("!=?")
    private static int $notEqualsNumber(@CommandSource ServerCommandSource source, ServerPlayerEntity player, double value, GreedyString numericValueProvider) {
        return compareNumericValue(source, player, value, numericValueProvider, (a, b) -> !Objects.equals(a, b));
    }

    @CommandNode(">?")
    private static int $greaterThanNumber(@CommandSource ServerCommandSource source, ServerPlayerEntity player, double value, GreedyString numericValueProvider) {
        return compareNumericValue(source, player, value, numericValueProvider, (a, b) -> a > b);
    }

    @CommandNode(">=?")
    private static int $greaterEqualsThanNumber(@CommandSource ServerCommandSource source, ServerPlayerEntity player, double value, GreedyString numericValueProvider) {
        return compareNumericValue(source, player, value, numericValueProvider, (a, b) -> a >= b);
    }

    @CommandNode("<?")
    private static int $lessThanNumber(@CommandSource ServerCommandSource source, ServerPlayerEntity player, double value, GreedyString numericValueProvider) {
        return compareNumericValue(source, player, value, numericValueProvider, (a, b) -> a < b);
    }

    @CommandNode("<=?")
    private static int $lessEqualsThanNumber(@CommandSource ServerCommandSource source, ServerPlayerEntity player, double value, GreedyString numericValueProvider) {
        return compareNumericValue(source, player, value, numericValueProvider, (a, b) -> a <= b);
    }

    private static int compareNumericValue(ServerCommandSource source, ServerPlayerEntity player, double value, GreedyString numericValueProvider, BiPredicate<Double, Double> predicate) {
        String $numericValueProvider = numericValueProvider.getValue();
        Text numericValueText = TextHelper.getTextByValue(player, $numericValueProvider);
        String numericValueString = TextHelper.Operators.visitString(numericValueText);
        try {
            double numericValue = Double.parseDouble(numericValueString);
            boolean testResult = predicate.test(value, numericValue);
            return CommandHelper.Return.returnBoolean(source, testResult);
        } catch (NumberFormatException e) {
            TextHelper.sendTextByKey(source, "placeholder.number.parse.failed", TextHelper.Parsers.escapeTags($numericValueProvider));
            return CommandHelper.Return.FAIL;
        }
    }
}

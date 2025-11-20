package mod.fuji.module.initializer.predicate;

import com.mojang.authlib.GameProfile;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import mod.fuji.core.auxiliary.minecraft.AuthlibHelper;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.InventoryHelper;
import mod.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import mod.fuji.core.auxiliary.minecraft.PlaceholderHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.Dimension;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.command.argument.wrapper.impl.OfflineGameProfile;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.document.descriptor.PermissionDescriptor;
import mod.fuji.core.document.structure.DocString;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.world.item.ItemStack;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

@Document(id = 1751826497994L, value = """
    This module provides a collection of `predicate commands`.
    They can be used to test the `conditions`.
    """)
@ColorBox(id = 1751978705157L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How it works?
    A `predicate command` will return an `integer value` to represent the `SUCCESS` or `FAILED` test result.
    It relies on the `return value of command` in vanilla Minecraft.
    See https://minecraft.fandom.com/wiki/Commands/return

    Actually, you can also use the `predicate command` with `command block`.
    """)
@ColorBox(id = 1751978789271L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Test a condition, and then run a command if success.
    Issue: `/run as player Alice chain has-perm? %player:name% 4 chain say value is true`
    """)
@ColorBox(id = 1754448424606L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Test if a player is holding an apple
    Issue: `/command-debug is-holding? Steve minecraft:apple`

    ◉ Test if a player has specified string permission
    Issue: `/command-debug has-perm? Steve fuji.permission.back`

    ◉ Test if a player's health >= 10
    Issue: `/command-debug <=? Steve 10 %player:health%`

    ◉ Test if a player killed more than 100 zombies.
    Issue: `/command-debug <=? Steve 100 %player:statistic_raw minecraft:killed minecraft:zombie%`

    ◉ Leverage the `vanilla Minecraft statistics`
    See: https://minecraft.fandom.com/wiki/Statistics

    ◉ More examples
    You can see more examples in `command_meta.IF` module and `command_meta.chain` module.
    """)


@CommandRequirement(level = 4)
public class PredicateInitializer extends ModuleInitializer {

    @CommandNode("has-perm?")
    private static int $hasPerm(@CommandSource CommandSourceStack source, OfflineGameProfile player, GreedyString stringPermission) {
        GameProfile gameProfile = player.getValue();
        boolean value = LuckpermsHelper.hasPermission(AuthlibHelper.getId(gameProfile), new PermissionDescriptor(true, stringPermission.getValue(), DocString.DUMMY_DOC_STRING_ID));
        return CommandHelper.Return.returnBoolean(source, value);
    }

    @Document(id = 1751826502598L, value = "Predicate to test if the player has the level-perm?")
    @CommandNode("has-level?")
    private static int $hasLevel(@CommandSource CommandSourceStack source, OfflineGameProfile player, int levelPermission) {
        GameProfile gameProfile = player.getValue();
        boolean value = CommandHelper.Requirement.getPermissionLevel(gameProfile) >= levelPermission;
        return CommandHelper.Return.returnBoolean(source, value);
    }

    @Document(id = 1751826504480L, value = "Predicate if online players >= n.")
    @CommandNode("has-players?")
    private static int $hasPlayers(@CommandSource CommandSourceStack source, Optional<Integer> n) {
        int $n = n.orElse(0);
        boolean value = PlayerHelper.Lookup.getOnlinePlayers().size() >= $n;
        return CommandHelper.Return.returnBoolean(source, value);
    }

    @CommandNode("is-op?")
    private static int $isOp(@CommandSource CommandSourceStack source, ServerPlayer player) {
        boolean value = CommandHelper.Requirement.isOperator(player);
        return CommandHelper.Return.returnBoolean(source, value);
    }

    @CommandNode("is-holding?")
    private static int $isHolding(@CommandSource CommandSourceStack source, ServerPlayer player, ItemPredicateArgument.Result itemPredicate) {
        boolean value = player.isHolding(itemPredicate);
        return CommandHelper.Return.returnBoolean(source, value);
    }

    @CommandNode("has-item?")
    private static int $hasItem(@CommandSource CommandSourceStack source, ServerPlayer player, ItemPredicateArgument.Result itemPredicate, int count) {
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
    private static int $hasExp(@CommandSource CommandSourceStack source, ServerPlayer player, int exp) {
        boolean value = player.totalExperience >= exp;
        return CommandHelper.Return.returnBoolean(source, value);
    }

    @CommandNode("has-exp-level?")
    private static int $hasExpLevel(@CommandSource CommandSourceStack source, ServerPlayer player, int expLevel) {
        boolean value = player.experienceLevel >= expLevel;
        return CommandHelper.Return.returnBoolean(source, value);
    }

    @CommandNode("is-in-world?")
    private static int $isInWorld(@CommandSource CommandSourceStack source, ServerPlayer player, Dimension dimension) {
        boolean value = EntityHelper.getServerWorld(player).equals(dimension.getValue());
        return CommandHelper.Return.returnBoolean(source, value);
    }

    @CommandNode("is-in-gamemode?")
    private static int $isInGameMode(@CommandSource CommandSourceStack source, ServerPlayer player, GameType gameMode) {
        boolean value = player.gameMode.getGameModeForPlayer().equals(gameMode);
        return CommandHelper.Return.returnBoolean(source, value);
    }

    @CommandNode("=?")
    private static int $equalsNumber(@CommandSource CommandSourceStack source, ServerPlayer player, double value, GreedyString numericValueString) {
        return compareNumericValue(source, player, value, numericValueString, Objects::equals);
    }

    @CommandNode("!=?")
    private static int $notEqualsNumber(@CommandSource CommandSourceStack source, ServerPlayer player, double value, GreedyString numericValueString) {
        return compareNumericValue(source, player, value, numericValueString, (a, b) -> !Objects.equals(a, b));
    }

    @CommandNode(">?")
    private static int $greaterThanNumber(@CommandSource CommandSourceStack source, ServerPlayer player, double value, GreedyString numericValueString) {
        return compareNumericValue(source, player, value, numericValueString, (a, b) -> a > b);
    }

    @CommandNode(">=?")
    private static int $greaterEqualsThanNumber(@CommandSource CommandSourceStack source, ServerPlayer player, double value, GreedyString numericValueString) {
        return compareNumericValue(source, player, value, numericValueString, (a, b) -> a >= b);
    }

    @CommandNode("<?")
    private static int $lessThanNumber(@CommandSource CommandSourceStack source, ServerPlayer player, double value, GreedyString numericValueString) {
        return compareNumericValue(source, player, value, numericValueString, (a, b) -> a < b);
    }

    @CommandNode("<=?")
    private static int $lessEqualsThanNumber(@CommandSource CommandSourceStack source, ServerPlayer player, double value, GreedyString numericValueString) {
        return compareNumericValue(source, player, value, numericValueString, (a, b) -> a <= b);
    }

    private static int compareNumericValue(CommandSourceStack source, ServerPlayer player, double value, GreedyString placeholderString, BiPredicate<Double, Double> predicate) {
        String numericValueString = PlaceholderHelper.parsePlaceholderString(player, placeholderString.getValue());
        try {
            double numericValue = Double.parseDouble(numericValueString);
            boolean testResult = predicate.test(value, numericValue);
            return CommandHelper.Return.returnBoolean(source, testResult);
        } catch (NumberFormatException e) {
            TextHelper.sendTextByKey(source, "placeholder.number.parse.failed", TextHelper.Parsers.escapeTags(numericValueString));
            return CommandHelper.Return.FAILURE;
        }
    }

    @CommandNode("equals?")
    private static int $equals(@CommandSource CommandSourceStack source, ServerPlayer player, String expectedString, GreedyString placeholderString) {
        return compareStringValue(source, player, expectedString, placeholderString, String::equals);
    }

    @CommandNode("true?")
    private static int $true(@CommandSource CommandSourceStack source, ServerPlayer player, GreedyString placeholderString) {
        return $equals(source, player, "true", placeholderString);
    }

    @CommandNode("false?")
    private static int $false(@CommandSource CommandSourceStack source, ServerPlayer player, GreedyString placeholderString) {
        return $equals(source, player, "false", placeholderString);
    }

    @CommandNode("matches?")
    private static int $matches(@CommandSource CommandSourceStack source, ServerPlayer player, String expectedString, GreedyString placeholderString) {
        return compareStringValue(source, player, expectedString, placeholderString, (a, b) -> b.matches(a));
    }

    private static int compareStringValue(CommandSourceStack source, ServerPlayer player, String value, GreedyString placeholderString, BiPredicate<String, String> predicate) {
        String placeholderValue = PlaceholderHelper.parsePlaceholderString(player, placeholderString.getValue());
        boolean test = predicate.test(value, placeholderValue);
        return CommandHelper.Return.returnBoolean(source, test);
    }

}

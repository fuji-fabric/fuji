package io.github.sakurawald.fuji.module.initializer.command_cooldown;

import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlaceholderHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.descriptor.PlaceholderDescriptor;
import io.github.sakurawald.fuji.core.service.duration_parser.DurationParser;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.service.NamedCooldownService;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.structure.NamedCommandCooldown;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class CommandCooldownPlaceholders {

    private static final MutableText NOT_COOLDOWN_FOUND_ERROR_TEXT = Text.literal("NOT_COOLDOWN_FOUND_ERROR");

    @DocStringProvider(id = 1751999791863L, value = """
        Returns the `remaining available uses` for `specified named cooldown` in integer.

        For example, if you have a `named cooldown` whose name is `kitfood`.
        You can use: `%fuji:command_cooldown_left_usage kitfood%`
        """)
    static void registerCommandCooldownLeftUsagePlaceholder() {
        PlaceholderDescriptor leftUsageDescriptor = new PlaceholderDescriptor("command_cooldown_left_usage", 1751999791863L);
        PlaceholderHelper.registerPlayerPlaceholder(leftUsageDescriptor, (player, args) -> {
            NamedCommandCooldown cooldown = NamedCooldownService.getNamedCooldownList().get(args);
            if (cooldown == null) return NOT_COOLDOWN_FOUND_ERROR_TEXT;

            String key = PlayerHelper.getPlayerName(player);
            int uses = cooldown.getUses().computeIfAbsent(key, k -> 0);
            int availableUses = cooldown.getMaxUses() - uses;
            return Text.literal(String.valueOf(availableUses));
        });
    }

    @DocStringProvider(id = 1751999769680L, value = """
        Returns the `remaining cooldown duration` for `specified named cooldown` in `1d2h3m4s` format.

        For example, if you have a `named cooldown` whose name is `kitfood`.
        You can use: `%fuji:command_cooldown_left_time kitfood%`
        """)
    static void registerCommandCooldownLeftTimePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("command_cooldown_left_time", 1751999769680L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player, args) -> {
            NamedCommandCooldown cooldown = NamedCooldownService.getNamedCooldownList().get(args);
            if (cooldown == null) return NOT_COOLDOWN_FOUND_ERROR_TEXT;

            String key = PlayerHelper.getPlayerName(player);
            long remainingDuration = cooldown.getRemainingTime(key, cooldown.getCooldownDuration());
            remainingDuration = Math.max(0, remainingDuration);
            String formattedRemainingDuration = DurationParser.formatDurationIntoCompact(remainingDuration);
            return Text.literal(formattedRemainingDuration);
        });
    }

    @DocStringProvider(id = 1752625269482L, value = """
        Returns the `next available date` for `specified named cooldown`.

        For example, if you have a `named cooldown` whose name is `kitfood`.
        You can use: `%fuji:command_cooldown_left_time_date kitfood%`
        """)
    static void registerCommandCooldownLeftTimeDatePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("command_cooldown_left_time_date", 1752625269482L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player, args) -> {
            NamedCommandCooldown cooldown = NamedCooldownService.getNamedCooldownList().get(args);
            if (cooldown == null) return NOT_COOLDOWN_FOUND_ERROR_TEXT;

            String key = PlayerHelper.getPlayerName(player);
            long nextAvailableDate = cooldown.getLastUseTime(key) + cooldown.getCooldownDuration();
            String formattedLeftTime = ChronosUtil.toDefaultDateFormat(nextAvailableDate);
            return Text.literal(formattedLeftTime);
        });
    }
}

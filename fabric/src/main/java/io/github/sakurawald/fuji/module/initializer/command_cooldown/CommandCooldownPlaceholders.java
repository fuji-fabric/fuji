package io.github.sakurawald.fuji.module.initializer.command_cooldown;

import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlaceholderHelper;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.descriptor.PlaceholderDescriptor;
import io.github.sakurawald.fuji.core.service.duration_parser.DurationParser;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.service.NamedCooldownService;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.structure.NamedCooldownDescriptor;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.structure.NamedCooldownDataNode;
import java.util.function.Function;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandCooldownPlaceholders {

    private static Text makeUnknownNamedCooldownText(String cooldownName) {
        return Text.literal("[Unknown named-cooldown: %s]".formatted(cooldownName));
    }

    private static Text mapNamedCooldownDataNodeIntoText(@Nullable String args, @NotNull Function<NamedCooldownDataNode, Text> mapper) {
        NamedCooldownDescriptor cooldownDescriptor = NamedCooldownService.getNamedCooldownDescriptors().get(args);
        if (cooldownDescriptor == null) {
            return makeUnknownNamedCooldownText(args);
        }
        return NamedCooldownService.withNamedCooldownDataNode(cooldownDescriptor, mapper);
    }

    @DocStringProvider(id = 1751999791863L, value = """
        Returns the `remaining available uses` for `specified named cooldown` in integer.

        For example, if you have a `named cooldown` whose name is `kitfood`.
        You can use: `%fuji:command_cooldown_left_usage kitfood%`
        """)
    static void registerCommandCooldownLeftUsagePlaceholder() {
        PlaceholderDescriptor placeholderDescriptor = new PlaceholderDescriptor("command_cooldown_left_usage", 1751999791863L);
        PlaceholderHelper.registerPlayerPlaceholder(placeholderDescriptor, (player, args) -> mapNamedCooldownDataNodeIntoText(args, dataNode -> {
            String key = NamedCooldownDataNode.toKey(player);
            int uses = dataNode.getUses().computeIfAbsent(key, k -> 0);
            int availableUses = dataNode.getDescriptor().getMaxUses() - uses;
            return Text.literal(String.valueOf(availableUses));
        }));
    }

    @DocStringProvider(id = 1751999769680L, value = """
        Returns the `remaining cooldown duration` for `specified named cooldown` in `1d2h3m4s` format.

        For example, if you have a `named cooldown` whose name is `kitfood`.
        You can use: `%fuji:command_cooldown_left_time kitfood%`
        """)
    static void registerCommandCooldownLeftTimePlaceholder() {
        PlaceholderDescriptor placeholderDescriptor = new PlaceholderDescriptor("command_cooldown_left_time", 1751999769680L);
        PlaceholderHelper.registerPlayerPlaceholder(placeholderDescriptor, (player, args) -> mapNamedCooldownDataNodeIntoText(args, dataNode -> {
            String key = NamedCooldownDataNode.toKey(player);
            long remainingDuration = dataNode.getRemainingTime(key, dataNode.getDescriptor().getCooldownDuration());
            remainingDuration = Math.max(0, remainingDuration);
            String formattedRemainingDuration = DurationParser.formatDurationIntoCompact(remainingDuration);
            return Text.literal(formattedRemainingDuration);
        }));
    }

    @DocStringProvider(id = 1752625269482L, value = """
        Returns the `next available date` for `specified named cooldown`.

        For example, if you have a `named cooldown` whose name is `kitfood`.
        You can use: `%fuji:command_cooldown_left_time_date kitfood%`
        """)
    static void registerCommandCooldownLeftTimeDatePlaceholder() {
        PlaceholderDescriptor placeholderDescriptor = new PlaceholderDescriptor("command_cooldown_left_time_date", 1752625269482L);
        PlaceholderHelper.registerPlayerPlaceholder(placeholderDescriptor, (player, args) -> mapNamedCooldownDataNodeIntoText(args, dataNode -> {
            String key = NamedCooldownDataNode.toKey(player);
            long nextAvailableDate = dataNode.getCooldown().getLastUseTime(key) + dataNode.getDescriptor().getCooldownDuration();
            String formattedLeftTime = ChronosUtil.Formatter.formatDate(nextAvailableDate);
            return Text.literal(formattedLeftTime);
        }));
    }
}

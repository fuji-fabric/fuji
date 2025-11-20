package mod.fuji.module.initializer.warning;

import mod.fuji.core.auxiliary.ChronosUtil;
import mod.fuji.core.auxiliary.minecraft.PlaceholderHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.document.annotation.DocStringProvider;
import mod.fuji.core.document.descriptor.PlaceholderDescriptor;
import mod.fuji.module.initializer.warning.service.WarningService;
import mod.fuji.module.initializer.warning.structure.Warning;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class WarningPlaceholders {

    @DocStringProvider(id = 1754640787907L, value = "Returns the `created date` of the last created warning.")
    public static void registerLastWarningCreatedDatePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("last_warning_created_date", 1754640787907L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> mapWarningIntoText(player, it -> Component.nullToEmpty(ChronosUtil.Formatter.formatDate(it.getCreatedTimestamp()))));
    }

    @DocStringProvider(id = 1754641490973L, value = "Returns the `creator name` of the last created warning.")
    public static void registerLastWarningCreatorNamePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("last_warning_creator_name", 1754641490973L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> mapWarningIntoText(player, it -> Component.nullToEmpty(it.getCreatorName())));
    }

    @DocStringProvider(id = 1754641445726L, value = "Returns the `expiration date` of the last created warning.")
    public static void registerLastWarningExpirationDatePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("last_warning_expiration_date", 1754641445726L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> mapWarningIntoText(player, it -> Component.nullToEmpty(ChronosUtil.Formatter.formatDate(it.getExpirationTimestamp()))));
    }

    @DocStringProvider(id = 1754641570588L, value = "Returns the `reason` of the last created warning.")
    public static void registerLastWarningReasonPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("last_warning_reason", 1754641570588L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> mapWarningIntoText(player, it -> Component.nullToEmpty(it.getDescription())));
    }

    private static @NotNull Component mapWarningIntoText(ServerPlayer player, Function<Warning, Component> mapper) {
        String playerName = PlayerHelper.getPlayerName(player);
        Optional<Warning> first = WarningService
            .getPlayerWarnings(playerName)
            .getWarnings()
            .stream()
            .max(Comparator.comparing(Warning::getCreatedTimestamp));

        return first
            .map(mapper)
            .orElseGet(() -> TextHelper.getTextByKey(player, "entity.none"));
    }


}

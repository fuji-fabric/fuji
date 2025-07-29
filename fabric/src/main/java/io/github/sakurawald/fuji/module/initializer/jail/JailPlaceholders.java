package io.github.sakurawald.fuji.module.initializer.jail;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlaceholderHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.descriptor.PlaceholderDescriptor;
import io.github.sakurawald.fuji.module.initializer.jail.service.JailService;
import io.github.sakurawald.fuji.module.initializer.jail.structure.JailRecord;
import java.util.function.Function;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

@SuppressWarnings("CodeBlock2Expr")
public class JailPlaceholders {

    @DocStringProvider(id = 1753756120399L, value = """
        Returns the `jail id` of the jail the player is currently in.
        """)
    public static void registerCurrentJailId() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("jail_id", 1753756120399L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> {
            return getJailRecordText(player, it -> Text.of(it.getOwnerJailDescriptor().getId()));
        });
    }

    @DocStringProvider(id = 1753755078104L, value = """
        Returns the `jail display name` of the jail the player is currently in.
        """)
    public static void registerCurrentJailDisplayName() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("jail_displayname", 1753755078104L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> {
            return getJailRecordText(player, it -> TextHelper.getTextByValue(player, it.getOwnerJailDescriptor().getDisplayName()));
        });
    }

    @DocStringProvider(id = 1753756207503L, value = """
        Returns the `creator name` of the `jail record` the player is currently active.
        """)
    public static void registerCurrentJailCreatorName() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("jail_creator_name", 1753756207503L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> {
            return getJailRecordText(player, it -> Text.of(it.getCreatorName()));
        });
    }

    @DocStringProvider(id = 1753756370321L, value = """
        Returns the `remaining jail duration` of the `jail record` the player is currently active.
        """)
    public static void registerCurrentJailRemainingJailDuration() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("jail_remaining_duration", 1753756370321L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> {
            return getJailRecordText(player, it -> Text.of(it.getRemainingJailDuration()));
        });
    }

    @DocStringProvider(id = 1753756478336L, value = """
        Returns the `specified jail duration` of the `jail record` the player is currently active.
        """)
    public static void registerCurrentJailSpecifiedJailDuration() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("jail_specified_duration", 1753756478336L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor,(player) -> {
            return getJailRecordText(player, it -> Text.of(it.getSpecifiedJailDuration()));
        });
    }

    @DocStringProvider(id = 1753756541250L, value = """
        Returns the `reason` of the active `jail record`.
        """)
    public static void registerCurrentJailReason() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("jail_reason", 1753756541250L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> {
            return getJailRecordText(player, it -> Text.of(it.getReason()));
        });
    }

    @DocStringProvider(id = 1753756611853L, value = """
        Returns the `created date` of the active `jail record`.
        """)
    public static void registerCurrentJailCreatedDate() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("jail_created_date", 1753756611853L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> {
            return getJailRecordText(player, it -> Text.of(it.getFormattedCreatedTimestamp()));
        });
    }

    private static Text getJailRecordText(ServerPlayerEntity player, Function<JailRecord, Text> jailRecordMapper) {
        String playerName= PlayerHelper.getPlayerName(player);
        return JailService.getCurrentJailRecord(playerName)
            .map(jailRecordMapper)
            .orElseGet(JailService::getNoJailStatusText);
    }

}

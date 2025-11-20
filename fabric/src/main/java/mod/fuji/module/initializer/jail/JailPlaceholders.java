package mod.fuji.module.initializer.jail;

import mod.fuji.core.auxiliary.minecraft.PlaceholderHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.document.annotation.DocStringProvider;
import mod.fuji.core.document.descriptor.PlaceholderDescriptor;
import mod.fuji.module.initializer.jail.service.JailService;
import mod.fuji.module.initializer.jail.structure.JailRecord;
import java.util.function.Function;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

@SuppressWarnings("CodeBlock2Expr")
public class JailPlaceholders {

    @DocStringProvider(id = 1753756120399L, value = """
        Returns the `jail id` from the player's active `jail record`.
        """)
    public static void registerJailIdPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("jail_id", 1753756120399L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> {
            return getJailRecordText(player, it -> Component.nullToEmpty(it.getOwnerJailDescriptor().getId()));
        });
    }

    @DocStringProvider(id = 1753755078104L, value = """
        Returns the `jail display name` from the player's active `jail record`.
        """)
    public static void registerJailDisplayNamePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("jail_displayname", 1753755078104L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> {
            return getJailRecordText(player, it -> TextHelper.getTextByValue(player, it.getOwnerJailDescriptor().getDisplayName()));
        });
    }

    @DocStringProvider(id = 1753756207503L, value = """
        Returns the `creator name` from the player's active `jail record`.
        """)
    public static void registerJailCreatorNamePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("jail_creator_name", 1753756207503L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> {
            return getJailRecordText(player, it -> Component.nullToEmpty(it.getCreatorName()));
        });
    }

    @DocStringProvider(id = 1753756370321L, value = """
        Returns the `remaining jail duration` from the player's active `jail record`.
        """)
    public static void registerJailRemainingJailDurationPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("jail_remaining_duration", 1753756370321L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> {
            return getJailRecordText(player, it -> Component.nullToEmpty(it.getRemainingJailDuration()));
        });
    }

    @DocStringProvider(id = 1753756478336L, value = """
        Returns the `specified jail duration` from the player's active `jail record`.
        """)
    public static void registerJailSpecifiedJailDurationPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("jail_specified_duration", 1753756478336L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor,(player) -> {
            return getJailRecordText(player, it -> Component.nullToEmpty(it.getSpecifiedJailDuration()));
        });
    }

    @DocStringProvider(id = 1753756541250L, value = """
        Returns the `reason` from the player's active `jail record`.
        """)
    public static void registerJailReasonPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("jail_reason", 1753756541250L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> {
            return getJailRecordText(player, it -> Component.nullToEmpty(it.getReason()));
        });
    }

    @DocStringProvider(id = 1753756611853L, value = """
        Returns the `created date` from the player's active `jail record`.
        """)
    public static void registerJailCreatedDatePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("jail_created_date", 1753756611853L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> {
            return getJailRecordText(player, it -> Component.nullToEmpty(it.getFormattedCreatedTimestamp()));
        });
    }

    @DocStringProvider(id = 1753759462210L, value = """
        Returns the `dimension` from the player's active `jail record`.
        """)
    public static void registerJailDimensionPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("jail_dimension", 1753759462210L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> {
            return getJailRecordText(player, it -> Component.nullToEmpty(it.getOwnerJailDescriptor().getGlobalPosition().getLevel()));
        });
    }

    @DocStringProvider(id = 1753759553388L, value = """
        Returns the `position x` from the player's active `jail record`.
        """)
    public static void registerJailXPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("jail_x", 1753759553388L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> {
            return getJailRecordText(player, it -> Component.nullToEmpty(String.valueOf(it.getOwnerJailDescriptor().getGlobalPosition().getX())));
        });
    }

    @DocStringProvider(id = 1753759629341L, value = """
        Returns the `position y` from the player's active `jail record`.
        """)
    public static void registerJailYPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("jail_y", 1753759629341L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> {
            return getJailRecordText(player, it -> Component.nullToEmpty(String.valueOf(it.getOwnerJailDescriptor().getGlobalPosition().getY())));
        });
    }
    @DocStringProvider(id = 1753759649983L, value = """
        Returns the `position z` from the player's active `jail record`.
        """)
    public static void registerJailZPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("jail_z", 1753759649983L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> {
            return getJailRecordText(player, it -> Component.nullToEmpty(String.valueOf(it.getOwnerJailDescriptor().getGlobalPosition().getZ())));
        });
    }
    @DocStringProvider(id = 1753759662834L, value = """
        Returns the `position yaw` from the player's active `jail record`.
        """)
    public static void registerJailYawPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("jail_yaw", 1753759662834L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> {
            return getJailRecordText(player, it -> Component.nullToEmpty(String.valueOf(it.getOwnerJailDescriptor().getGlobalPosition().getYaw())));
        });
    }
    @DocStringProvider(id = 1753759691598L, value = """
        Returns the `position pitch` from the player's active `jail record`.
        """)
    public static void registerJailPitchPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("jail_pitch", 1753759691598L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> {
            return getJailRecordText(player, it -> Component.nullToEmpty(String.valueOf(it.getOwnerJailDescriptor().getGlobalPosition().getPitch())));
        });
    }


    private static Component getJailRecordText(ServerPlayer player, Function<JailRecord, Component> jailRecordMapper) {
        String playerName= PlayerHelper.getPlayerName(player);
        return JailService.getActiveJailRecord(playerName)
            .map(jailRecordMapper)
            .orElseGet(JailService::getNoJailStatusText);
    }


}

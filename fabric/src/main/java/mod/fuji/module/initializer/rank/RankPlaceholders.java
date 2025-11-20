package mod.fuji.module.initializer.rank;

import mod.fuji.core.auxiliary.minecraft.PlaceholderHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.document.annotation.DocStringProvider;
import mod.fuji.core.document.descriptor.PlaceholderDescriptor;
import mod.fuji.module.initializer.rank.service.RankService;
import mod.fuji.module.initializer.rank.structure.RankNode;
import java.util.function.Function;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class RankPlaceholders {

    @DocStringProvider(id = 1754432388361L, value = """
        Returns the `rank id` of the player's current rank.
        """)
    public static void registerRankIdPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("rank_id", 1754432388361L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> mapIntoText(player, it -> Component.nullToEmpty(it.getId())));
    }

    @DocStringProvider(id = 1754432587319L, value = """
        Returns the `rank display name` of the player's current rank.
        """)
    public static void registerRankDisplayNamePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("rank_displayname", 1754432587319L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> mapIntoText(player, it -> TextHelper.getTextByValue(null, it.getDisplayName())));
    }

    @DocStringProvider(id = 1754432726443L, value = """
        Returns the `rank display name raw string` of the player's current rank.
        """)
    public static void registerRankDisplayNameRawPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("rank_displayname_raw", 1754432726443L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> mapIntoText(player, it -> Component.nullToEmpty(it.getDisplayName())));
    }

    private static @NotNull Component mapIntoText(@NotNull ServerPlayer player, @NotNull Function<RankNode, Component> mapper) {
        return RankService
            .getCurrentRankNode(player)
            .map(mapper)
            .orElseGet(RankService::getNoRankStatusText);
    }


}

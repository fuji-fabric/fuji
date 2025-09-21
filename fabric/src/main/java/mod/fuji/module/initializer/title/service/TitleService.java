package mod.fuji.module.initializer.title.service;

import mod.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import mod.fuji.core.auxiliary.minecraft.PlaceholderHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.document.annotation.DocStringProvider;
import mod.fuji.core.document.descriptor.PermissionDescriptor;
import mod.fuji.core.document.descriptor.PlaceholderDescriptor;
import mod.fuji.module.initializer.title.TitleInitializer;
import mod.fuji.module.initializer.title.structure.TitleDescriptor;
import mod.fuji.module.initializer.title.structure.TitlePreference;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TitleService {

    @DocStringProvider(id = 1753000916378L, value = """
        Having this permission means obtaining a `title` with the specified `id`.
        """)
    private static final PermissionDescriptor TITLE_OBTAINED_PERMISSION_DESCRIPTOR = new PermissionDescriptor("fuji.title.obtain.<title-id>", 1753000916378L);

    @DocStringProvider(id = 1753006644552L, value = """
        Returns the `display name` of `the active title` for the player.
        """)
    private static final PlaceholderDescriptor ACTIVE_TITLE_PLACEHOLDER_DESCRIPTOR = new PlaceholderDescriptor("active_title", 1753006644552L);

    public static List<TitleDescriptor> getAllTitles() {
        return TitleInitializer.config.model().getTitleDescriptors();
    }

    public static boolean isTitleObtained(ServerPlayerEntity player, String titleId) {
        return LuckpermsHelper.hasPermission(player.getUuid(), TITLE_OBTAINED_PERMISSION_DESCRIPTOR, titleId);
    }

    public static Optional<TitleDescriptor> getActiveTitle(ServerPlayerEntity player) {
        return withPreference(player, (preference) -> getTitle(preference.getActiveTitleId()));
    }

    public static void setActiveTitle(ServerPlayerEntity player, @Nullable String titleId) {
        withPreference(player, (preference) -> {
            preference.setActiveTitleId(titleId);
            return null;
        });
    }

    public static <T> T withPreference(ServerPlayerEntity player, Function<TitlePreference, T> function) {
        List<TitlePreference> preferences = TitleInitializer.data.model().getPreferences();
        String playerName = PlayerHelper.getPlayerName(player);
        Optional<TitlePreference> first = preferences
            .stream()
            .filter(it -> it.getPlayer().equals(playerName))
            .findFirst();

        TitlePreference titlePreference = first.orElseGet(() -> {
            TitlePreference temp = new TitlePreference();
            temp.setPlayer(playerName);
            temp.setActiveTitleId(getDefaultActiveTitleId());
            preferences.add(temp);
            return temp;
        });

        T value = function.apply(titlePreference);
        TitleInitializer.data.writeStorage();
        return value;
    }

    public static @Nullable String getDefaultActiveTitleId() {
        return TitleInitializer.config.model().getDefaultActiveTitleId();
    }

    public static @NotNull String getNoTitleActiveText() {
        return TitleInitializer.config.model().getNoActiveTitleText();
    }

    public static List<TitleDescriptor> getObtainedTitles(ServerPlayerEntity player) {
        return getAllTitles()
            .stream()
            .filter(descriptor -> isTitleObtained(player, descriptor.getId()))
            .toList();
    }

    public static Optional<TitleDescriptor> getTitle(String titleId) {
        return getAllTitles()
            .stream()
            .filter(descriptor ->  descriptor.getId().equals(titleId))
            .findFirst();
    }

    public static void registerActiveTitlePlaceholder() {
        PlaceholderHelper.registerPlayerPlaceholder(ACTIVE_TITLE_PLACEHOLDER_DESCRIPTOR, (player) -> {
            Optional<TitleDescriptor> activeTitle = TitleService.getActiveTitle(player);
            if (activeTitle.isPresent()) {
                TitleDescriptor titleDescriptor = activeTitle.get();

                MutableText titleText = TextHelper.getTextByValue(player, titleDescriptor.getDisplayName()).copy();

                List<Text> loreTextList = TextHelper.getTextListByValue(player, titleDescriptor.getLore());
                MutableText hoverText = TextHelper.Operators.condenseTextList(loreTextList);
                titleText.fillStyle(Style
                    .EMPTY
                    .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(hoverText)));

                return titleText;
            }

            return TextHelper.getTextByValue(player, TitleService.getNoTitleActiveText());
        });
    }

}

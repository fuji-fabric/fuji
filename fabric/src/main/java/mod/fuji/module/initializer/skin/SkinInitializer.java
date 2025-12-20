package mod.fuji.module.initializer.skin;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.document.annotation.Cite;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.Word;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.skin.command.argument.wrapper.DefaultSkinName;
import mod.fuji.module.initializer.skin.config.model.SkinConfigModel;
import mod.fuji.module.initializer.skin.config.model.SkinDataModel;
import mod.fuji.module.initializer.skin.gui.SkinGui;
import mod.fuji.module.initializer.skin.provider.MineSkinSkinProvider;
import mod.fuji.core.service.gameprofile_fetcher.MojangSkinProvider;
import mod.fuji.module.initializer.skin.service.SkinService;
import mod.fuji.module.initializer.skin.structure.SkinVariant;
import net.minecraft.server.level.ServerPlayer;

@Document(id = 1751826807167L, value = """
    This module provides the `skin` management for players.
    """)
@ColorBox(id = 1753325192275L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How it works?
    This module provides the `skin` management:
    1. This module will `modify the skin` when the the player `login the server`.
    1.a. If there is existing `skin data` for this `player`, then we use that data.
    1.b. If there is no existing `skin data` for this `player`.
    1.b.i. If the `apply_default_skin_if_no_data` option is `true`, then we use `default skin` defined in the config file.
    1.b.ii. If the `apply_default_skin_if_no_data` option is `false`, then we fetch the skin from `Mojang online server`.

    2. A player can use `/skin` command to `change the skin` in-game.
    """)
@ColorBox(id = 1751979120689L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Use the `Mojang skin`.
    Issue: `/skin use-my-mojang-skin` to use your own skin.
    Issue: `/skin use-mojang-skin Alice` to use another player's skin.
    <red>NOTE: This requires fetching the skin from the Mojang server, which may be time-consuming.

    ◉ Set a skin from custom URL
    Issue: `/skin use-url-skin alex https://s.namemc.com/i/2af8c11db5fe6061.png`

    ◉ Use a `specified skin name` from the `default skin list` defined in the config file.
    Issue: `/skin use-default-skin reimu-hakurei`

    ◉ Use a `random` skin from the `default skin list` defined in the config file.
    Issue: `/skin use-random-default-skins`
    """)
@Cite("https://github.com/Suiranoil/SkinRestorer")
@CommandNode("skin")
public class SkinInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<SkinConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, SkinConfigModel.class);
    public static final BaseConfigurationHandler<SkinDataModel> data = ObjectConfigurationHandler.ofModule("skin-data.json", SkinDataModel.class);

    @Document(id = 1751826809279L, value = "Set skin to a random default skin.")
    @CommandNode("use-random-default-skins")
    private static int $useRandomDefaultSkins(@CommandSource @CommandTarget ServerPlayer player) {
        SkinService.changeSkin(player, SkinService::getRandomDefaultSkin);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1753333877248L, value = "Use the `default skin` with specified `skin name`.")
    @CommandNode("use-default-skin")
    private static int $useDefaultSkin(@CommandSource @CommandTarget ServerPlayer player, DefaultSkinName defaultSkinName) {
        String $defaultSkinName = defaultSkinName.getValue();
        return SkinService.findSkinDescriptor($defaultSkinName)
            .map(skinDescriptor -> {
                SkinService.changeSkin(player, () -> skinDescriptor.getSkinProperty().toNative());
                return CommandHelper.Return.SUCCESS;
            })
            .orElseGet(() -> {
                TextHelper.sendTextByKey(player, "skin.default_skin.unknown", $defaultSkinName);
                return CommandHelper.Return.FAILURE;
            });
    }

    @Document(id = 1753336550266L, value = """
        Open the `skin` GUI.
        """)
    @CommandNode("gui")
    private static int $gui(@CommandSource ServerPlayer player) {
        SkinGui
            .makeInstance(player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1753337059662L, value = """
        Alias to `/skin gui` command.
        """)
    @CommandNode
    private static int $skin(@CommandSource ServerPlayer player) {
        return $gui(player);
    }

    @Document(id = 1751826814466L, value = "Set skin to an online skin of the same name.")
    @CommandNode("use-my-mojang-skin")
    private static int $useMyMojangSkin(@CommandSource @CommandTarget ServerPlayer player) {
        String onlinePlayerName = PlayerHelper.getPlayerName(player);
        SkinService.changeSkin(player, () -> MojangSkinProvider.fetchSkin(onlinePlayerName).orElse(null));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1753250702541L, value = "Set skin to an online skin of the specified name.")
    @CommandNode("use-mojang-skin")
    private static int $useMojangSkin(@CommandSource @CommandTarget ServerPlayer player, Word skinName) {
        SkinService.changeSkin(player, () -> MojangSkinProvider.fetchSkin(skinName.getValue()).orElse(null));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826819277L, value = "Set skin to a custom url in Steve model.")
    @CommandNode("use-url-skin steve")
    private static int $useUrlSkinSteveModel(@CommandSource @CommandTarget ServerPlayer player, GreedyString url) {
        String $url = url.getValue();
        SkinService.changeSkin(player, () -> MineSkinSkinProvider.fetchSkin($url, SkinVariant.CLASSIC).orElse(null));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826827369L, value = "Set skin to a custom url in Alex model.")
    @CommandNode("use-url-skin alex")
    private static int $useUrlSkinAlexModel(@CommandSource @CommandTarget ServerPlayer player, GreedyString url) {
        String $url = url.getValue();
        SkinService.changeSkin(player, () -> MineSkinSkinProvider.fetchSkin($url, SkinVariant.SLIM).orElse(null));
        return CommandHelper.Return.SUCCESS;
    }

}

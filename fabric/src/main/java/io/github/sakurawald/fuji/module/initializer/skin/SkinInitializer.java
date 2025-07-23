package io.github.sakurawald.fuji.module.initializer.skin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GameProfileCollection;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.Word;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.service.gameprofile_fetcher.MojangProfileFetcher;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.skin.config.model.SkinConfigModel;
import io.github.sakurawald.fuji.module.initializer.skin.provider.MineSkinSkinProvider;
import io.github.sakurawald.fuji.module.initializer.skin.service.SkinService;
import io.github.sakurawald.fuji.module.initializer.skin.structure.SkinVariant;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@Document(id = 1751826807167L, value = """
    Customize the skins of players.
    """)
@ColorBox(id = 1751979120689L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Set a skin from mojang
    Issue: `/skin set mojang dream`

    ◉ Set a skin from custom URL
    Issue: `/skin set web slim "https://s.namemc.com/i/bd53d152d0cd91d0.png"`

    ◉ Use the default skins defined by the server
    Issue: `/skin use-default-skins`

    ◉ Use the online skin of your player name.
    Issue: `/skin use-online-skin`
    """)


@CommandNode("skin")
public class SkinInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<SkinConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, SkinConfigModel.class);

    @Document(id = 1751826809279L, value = "Set skin to a random default skin.")
    @CommandNode("use-default-skins")
    private static int $useDefault(@CommandSource @CommandTarget ServerPlayerEntity player) {
        ServerCommandSource commandSource = player.getCommandSource();
        SkinService.applySkin(commandSource, SkinService::getDefaultSkin);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826814466L, value = "Set skin to an online skin of the same name.")
    @CommandNode("use-online-skin")
    private static int $useOnlineSkin(@CommandSource @CommandTarget ServerPlayerEntity player) {
        ServerCommandSource commandSource = player.getCommandSource();
        String onlinePlayerName = PlayerHelper.getPlayerName(player);
        SkinService.applySkin(commandSource, () -> MojangProfileFetcher.fetchOnlineSkin(onlinePlayerName));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1753250702541L, value = "Set skin to an online skin of the specified name.")
    @CommandNode("set mojang")
    private static int $setMojang(@CommandSource @CommandTarget ServerPlayerEntity player, Word skinName) {
        ServerCommandSource commandSource = player.getCommandSource();
        SkinService.applySkin(commandSource, () -> MojangProfileFetcher.fetchOnlineSkin(skinName.getValue()));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826819277L, value = "Set skin to a custom url in Steve model.")
    @CommandNode("set web classic")
    private static int $setWebClassic(@CommandSource @CommandTarget ServerPlayerEntity player, String url) {
        ServerCommandSource commandSource = player.getCommandSource();
        SkinService.applySkin(commandSource, () -> MineSkinSkinProvider.fetchSkin(url, SkinVariant.CLASSIC));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826827369L, value = "Set skin to a custom url in Alex model.")
    @CommandNode("set web slim")
    private static int $setWebSlim(@CommandSource @CommandTarget ServerPlayerEntity player, String url) {
        ServerCommandSource commandSource = player.getCommandSource();
        SkinService.applySkin(commandSource, () -> MineSkinSkinProvider.fetchSkin(url, SkinVariant.SLIM));
        return CommandHelper.Return.SUCCESS;
    }

}

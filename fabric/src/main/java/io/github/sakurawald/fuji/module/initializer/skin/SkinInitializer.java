package io.github.sakurawald.fuji.module.initializer.skin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
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
    private static int $useDefault(@CommandSource CommandContext<ServerCommandSource> ctx) {
        SkinService.applySkin(ctx.getSource(), () -> SkinService.getDefaultSkin());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826811767L, value = "Set skin to a random default skin.")
    @CommandNode("use-default-skins")
    @CommandRequirement(level = 4)
    private static int $useDefaultOthers(@CommandSource CommandContext<ServerCommandSource> ctx, GameProfileCollection target) {
        SkinService.applySkin(ctx.getSource(), target.getValue(), true, () -> SkinService.getDefaultSkin());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826814466L, value = "Set skin to an online skin of the same name.")
    @CommandNode("use-online-skin")
    private static int $useOnlineSkin(@CommandSource CommandContext<ServerCommandSource> ctx) {
        SkinService.applySkin(ctx.getSource(), () -> MojangProfileFetcher.fetchOnlineSkin(ctx.getSource().getName()));
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("set mojang")
    private static int $setMojang(@CommandSource CommandContext<ServerCommandSource> ctx, Word skinName) {
        SkinService.applySkin(ctx.getSource(), () -> MojangProfileFetcher.fetchOnlineSkin(skinName.getValue()));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826816766L, value = "Set skin to an online skin of a specified name.")
    @CommandNode("set mojang")
    @CommandRequirement(level = 4)
    private static int $setMojangTarget(@CommandSource CommandContext<ServerCommandSource> ctx, Word skinName, GameProfileCollection target) {
        SkinService.applySkin(ctx.getSource(), target.getValue(), true, () -> MojangProfileFetcher.fetchOnlineSkin(skinName.getValue()));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826819277L, value = "Set skin to a custom url in steve model.")
    @CommandNode("set web classic")
    private static int $setWebClassic(@CommandSource CommandContext<ServerCommandSource> ctx, String url) {
        SkinService.applySkin(ctx.getSource(), () -> MineSkinSkinProvider.fetchSkin(url, SkinVariant.CLASSIC));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826822477L, value = "Set skin to a custom url in steve model.")
    @CommandNode("set web classic")
    @CommandRequirement(level = 4)
    private static int $setWebClassicOthers(@CommandSource CommandContext<ServerCommandSource> ctx, String url, GameProfileCollection target) {
        SkinService.applySkin(ctx.getSource(), target.getValue(), true, () -> MineSkinSkinProvider.fetchSkin(StringArgumentType.getString(ctx, "url"), SkinVariant.CLASSIC));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826827369L, value = "Set skin to a custom url in alex model.")
    @CommandNode("set web slim")
    private static int $setWebSlim(@CommandSource CommandContext<ServerCommandSource> ctx, String url) {
        SkinService.applySkin(ctx.getSource(), () -> MineSkinSkinProvider.fetchSkin(url, SkinVariant.SLIM));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826829102L, value = "Set skin to a custom url in alex model.")
    @CommandNode("set web slim")
    @CommandRequirement(level = 4)
    private static int $setWebSlimOthers(@CommandSource CommandContext<ServerCommandSource> ctx, String url, GameProfileCollection target) {
        SkinService.applySkin(ctx.getSource(), target.getValue(), true, () -> MineSkinSkinProvider.fetchSkin(StringArgumentType.getString(ctx, "url"), SkinVariant.SLIM));
        return CommandHelper.Return.SUCCESS;
    }

}

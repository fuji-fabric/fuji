package io.github.sakurawald.module.initializer.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.argument.wrapper.impl.GameProfileCollection;
import io.github.sakurawald.core.command.argument.wrapper.impl.Word;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.service.gameprofile_fetcher.MojangProfileFetcher;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.skin.config.model.SkinConfigModel;
import io.github.sakurawald.module.initializer.skin.provider.MineSkinSkinProvider;
import io.github.sakurawald.module.initializer.skin.structure.SkinRestorer;
import io.github.sakurawald.module.initializer.skin.structure.SkinVariant;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

@CommandNode("skin")
public class SkinInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<SkinConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, SkinConfigModel.class);

    @CommandNode("use-default-skins")
    @Document("Set skin to a random default skin.")
    private static int useDefault(@CommandSource CommandContext<ServerCommandSource> ctx) {
        doSkin(ctx.getSource(), () -> SkinRestorer.getSkinStorage().getDefaultSkin());
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("use-default-skins")
    @Document("Set skin to a random default skin.")
    @CommandRequirement(level = 4)
    private static int useDefaultOthers(@CommandSource CommandContext<ServerCommandSource> ctx, GameProfileCollection target) {
        doSkin(ctx.getSource(), target.getValue(), true, () -> SkinRestorer.getSkinStorage().getDefaultSkin());
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("use-online-skin")
    @Document("Set skin to an online skin of the same name.")
    private static int useOnlineSkin(@CommandSource CommandContext<ServerCommandSource> ctx) {
        doSkin(ctx.getSource(), () -> MojangProfileFetcher.fetchOnlineSkin(ctx.getSource().getName()));
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("set mojang")
    private static int setMojang(@CommandSource CommandContext<ServerCommandSource> ctx, Word skinName) {
        doSkin(ctx.getSource(), () -> MojangProfileFetcher.fetchOnlineSkin(skinName.getValue()));
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("set mojang")
    @CommandRequirement(level = 4)
    @Document("Set skin to an online skin of a specified name.")
    private static int setMojangTarget(@CommandSource CommandContext<ServerCommandSource> ctx, Word skinName, GameProfileCollection target) {
        doSkin(ctx.getSource(), target.getValue(), true, () -> MojangProfileFetcher.fetchOnlineSkin(skinName.getValue()));
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("set web classic")
    @Document("Set skin to a custom url in steve model.")
    private static int setWebClassic(@CommandSource CommandContext<ServerCommandSource> ctx, String url) {
        doSkin(ctx.getSource(), () -> MineSkinSkinProvider.fetchSkin(url, SkinVariant.CLASSIC));
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("set web classic")
    @CommandRequirement(level = 4)
    @Document("Set skin to a custom url in steve model.")
    private static int setWebClassicOthers(@CommandSource CommandContext<ServerCommandSource> ctx, String url, GameProfileCollection target) {
        doSkin(ctx.getSource(), target.getValue(), true, () -> MineSkinSkinProvider.fetchSkin(StringArgumentType.getString(ctx, "url"), SkinVariant.CLASSIC));
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("set web slim")
    @Document("Set skin to a custom url in alex model.")
    private static int setWebSlim(@CommandSource CommandContext<ServerCommandSource> ctx, String url) {
        doSkin(ctx.getSource(), () -> MineSkinSkinProvider.fetchSkin(url, SkinVariant.SLIM));
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("set web slim")
    @CommandRequirement(level = 4)
    @Document("Set skin to a custom url in alex model.")
    private static int setWebSlimOthers(@CommandSource CommandContext<ServerCommandSource> ctx, String url, GameProfileCollection target) {
        doSkin(ctx.getSource(), target.getValue(), true, () -> MineSkinSkinProvider.fetchSkin(StringArgumentType.getString(ctx, "url"), SkinVariant.SLIM));
        return CommandHelper.Return.SUCCESS;
    }

    private static int doSkin(@NotNull ServerCommandSource src, @NotNull Collection<GameProfile> targets, boolean setByOperator, @NotNull Supplier<Property> skinSupplier) {
        SkinRestorer.setSkinAsync(src.getServer(), targets, skinSupplier).thenAccept(pair -> {
            Collection<ServerPlayerEntity> players = pair.left();
            Collection<GameProfile> profiles = pair.right();

            if (profiles.isEmpty()) {
                TextHelper.sendMessageByKey(src, "skin.action.failed");
                return;
            }

            /* feedback */
            if (setByOperator) {
                TextHelper.sendMessageByKey(src, "skin.action.affected_profile", String.join(", ", profiles.stream().map(GameProfile::getName).toList()));

                if (!players.isEmpty()) {
                    TextHelper.sendMessageByKey(src, "skin.action.affected_player", String.join(", ", players.stream().map(p -> p.getGameProfile().getName()).toList()));
                }
            } else {
                TextHelper.sendMessageByKey(src, "skin.action.ok");
            }

        });

        return targets.size();
    }

    private static int doSkin(@NotNull ServerCommandSource src, @NotNull Supplier<Property> skinSupplier) {
        if (src.getPlayer() == null) return CommandHelper.Return.FAIL;

        return doSkin(src, Collections.singleton(src.getPlayer().getGameProfile()), false, skinSupplier);
    }

}

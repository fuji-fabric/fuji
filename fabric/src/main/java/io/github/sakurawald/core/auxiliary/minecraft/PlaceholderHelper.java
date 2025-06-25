package io.github.sakurawald.core.auxiliary.minecraft;

import eu.pb4.placeholders.api.PlaceholderHandler;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import io.github.sakurawald.Fuji;
import io.github.sakurawald.core.structure.descriptor.PlaceholderDescriptor;
import lombok.experimental.UtilityClass;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;
import java.util.function.Function;

@UtilityClass
public class PlaceholderHelper {

    public static final Text INVALID_ARGS_ERROR_TEXT = Text.literal("[INVALID-ARGS-ERROR]");
    private static final Text NO_PLAYER_ERROR_TEXT = Text.literal("[NO-PLAYER-ERROR]");
    private static final Text NO_SERVER_ERROR_TEXT = Text.literal("[NO-SERVER-ERROR]");

    @SuppressWarnings("resource")
    public static void registerServerPlaceholder(PlaceholderDescriptor descriptor, BiFunction<MinecraftServer, String, Text> function) {
        PlaceholderHandler placeholderHandler = (ctx, arg) -> {
            if (ctx.server() == null) return PlaceholderResult.value(PlaceholderHelper.NO_SERVER_ERROR_TEXT);
            return PlaceholderResult.value(function.apply(ctx.server(), arg));
        };

        String name = descriptor.getString();
        Placeholders.register(Identifier.of(Fuji.MOD_ID, name), placeholderHandler);
    }

    public static void registerPlayerPlaceholder(String name, BiFunction<ServerPlayerEntity, String, Text> function) {
        PlaceholderHandler placeholderHandler = (ctx, arg) -> {
            if (ctx.player() == null) return PlaceholderResult.value(NO_PLAYER_ERROR_TEXT);
            return PlaceholderResult.value(function.apply(ctx.player(), arg));
        };

        Placeholders.register(Identifier.of(Fuji.MOD_ID, name), placeholderHandler);
    }

    public static void registerServerPlaceholder(PlaceholderDescriptor descriptor, Function<MinecraftServer, Text> function) {
        registerServerPlaceholder(descriptor, (server, args) -> function.apply(server));
    }

    public static void registerPlayerPlaceholder(String name, Function<ServerPlayerEntity, Text> function) {
        registerPlayerPlaceholder(name, (player, args) -> function.apply(player));
    }
}

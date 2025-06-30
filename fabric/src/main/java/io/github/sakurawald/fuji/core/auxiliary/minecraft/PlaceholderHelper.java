package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import eu.pb4.placeholders.api.PlaceholderHandler;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.document.descriptor.PlaceholderDescriptor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;
import java.util.function.Function;

public class PlaceholderHelper {

    public static final Text INVALID_ARGS_ERROR_TEXT = Text.literal("[INVALID-ARGS-ERROR]");
    private static final Text NO_PLAYER_ERROR_TEXT = Text.literal("[NO-PLAYER-ERROR]");
    private static final Text NO_SERVER_ERROR_TEXT = Text.literal("[NO-SERVER-ERROR]");

    @SuppressWarnings("resource")
    public static void registerServerPlaceholder(PlaceholderDescriptor descriptor, BiFunction<MinecraftServer, String, Text> function) {
        PlaceholderHandler placeholderHandler = (ctx, arg) -> {
            // NOTE: The `arg` should be verified by the placeholder itself.
            if (ctx.server() == null) {
                return PlaceholderResult.value(PlaceholderHelper.NO_SERVER_ERROR_TEXT);
            }
            Text resultText = function.apply(ctx.server(), arg);
            return PlaceholderResult.value(resultText);
        };

        String placeholderName = descriptor.getString();
        Placeholders.register(Identifier.of(Fuji.MOD_ID, placeholderName), placeholderHandler);
    }

    public static void registerPlayerPlaceholder(PlaceholderDescriptor descriptor, BiFunction<ServerPlayerEntity, String, Text> function) {
        PlaceholderHandler placeholderHandler = (ctx, arg) -> {
            if (ctx.player() == null) {
                return PlaceholderResult.value(NO_PLAYER_ERROR_TEXT);
            }
            Text resultText = function.apply(ctx.player(), arg);
            return PlaceholderResult.value(resultText);
        };

        String placeholderName = descriptor.getString();
        Placeholders.register(Identifier.of(Fuji.MOD_ID, placeholderName), placeholderHandler);
    }

    public static void registerServerPlaceholder(PlaceholderDescriptor descriptor, Function<MinecraftServer, Text> function) {
        registerServerPlaceholder(descriptor, (server, args) -> function.apply(server));
    }

    public static void registerPlayerPlaceholder(PlaceholderDescriptor descriptor, Function<ServerPlayerEntity, Text> function) {
        registerPlayerPlaceholder(descriptor, (player, args) -> function.apply(player));
    }
}

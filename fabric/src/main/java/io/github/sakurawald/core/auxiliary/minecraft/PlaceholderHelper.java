package io.github.sakurawald.core.auxiliary.minecraft;

import eu.pb4.placeholders.api.PlaceholderHandler;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import io.github.sakurawald.Fuji;
import lombok.experimental.UtilityClass;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;
import java.util.function.Function;

@UtilityClass
public class PlaceholderHelper {

    public static final Text INVALID_TEXT = Text.literal("[INVALID]");
    private static final Text NO_PLAYER_TEXT = Text.literal("[NO-PLAYER]");
    private static final Text NO_SERVER_TEXT = Text.literal("[NO-SERVER]");

    @SuppressWarnings("resource")
    public static void withServer(String name, BiFunction<MinecraftServer, String, Text> function) {
        PlaceholderHandler placeholderHandler = (ctx, arg) -> {
            if (ctx.server() == null) return PlaceholderResult.value(PlaceholderHelper.NO_SERVER_TEXT);
            return PlaceholderResult.value(function.apply(ctx.server(), arg));
        };

        Placeholders.register(Identifier.of(Fuji.MOD_ID, name), placeholderHandler);
    }

    public static void withPlayer(String name, BiFunction<ServerPlayerEntity, String, Text> function) {
        PlaceholderHandler placeholderHandler = (ctx, arg) -> {
            if (ctx.player() == null) return PlaceholderResult.value(NO_PLAYER_TEXT);
            return PlaceholderResult.value(function.apply(ctx.player(), arg));
        };

        Placeholders.register(Identifier.of(Fuji.MOD_ID, name), placeholderHandler);
    }

    public static void withServer(String name, Function<MinecraftServer, Text> function) {
        withServer(name, (server, args) -> function.apply(server));
    }

    public static void withPlayer(String name, Function<ServerPlayerEntity, Text> function) {
        withPlayer(name, (player, args) -> function.apply(player));
    }
}

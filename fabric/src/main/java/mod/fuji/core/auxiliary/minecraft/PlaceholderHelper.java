package mod.fuji.core.auxiliary.minecraft;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import mod.fuji.Fuji;
import mod.fuji.core.document.descriptor.PlaceholderDescriptor;
import mod.fuji.core.structure.IdentifierIR;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderHelper {

    private static final String INVALID_ARGS_ERROR_REASON = "INVALID-ARGS-ERROR";
    private static final String NO_PLAYER_ERROR_REASON = "NO-PLAYER-ERROR";
    private static final String NO_SERVER_ERROR_REASON = "NO-SERVER-ERROR";

    /**
     * The args may be null if the user didn't specify it.
     **/
    public static @NotNull List<String> splitArguments(@Nullable String args) {
        if (args == null) {
            return List.of();
        }
        args = args.trim();
        String[] split = args.split("\\s+");
        return List.of(split);
    }

    public static Component makeInvalidArgsErrorText() {
        return PlaceholderResult
            .invalid(INVALID_ARGS_ERROR_REASON)
            #if MC_VER < MC_26_1
            .text();
            #elif MC_VER >= MC_26_1
            .component();
            #endif

    }

    private static void registerPlaceholder(@NotNull PlaceholderDescriptor descriptor,
                                            #if MC_VER < MC_26_1
                                            @NotNull eu.pb4.placeholders.api.PlaceholderHandler
                                            #elif MC_VER >= MC_26_1
                                            @NotNull eu.pb4.placeholders.api.Placeholder.Handler<eu.pb4.placeholders.api.ServerPlaceholderContext, String>
                                            #endif
        placeholderHandler) {
        String placeholderName = descriptor.getString();
        IdentifierIR identifier = IdentifierIR.makeIdentifierOrThrow(Fuji.MOD_ID, placeholderName);

        #if MC_VER < MC_26_1
        Placeholders.register(identifier.getNativeValue(), placeholderHandler);
        #elif MC_VER >= MC_26_1
        Placeholders.registerServer(identifier.getNativeValue(), placeholderHandler);
        #endif

    }

    public static void registerGenericPlaceholder(@NotNull PlaceholderDescriptor descriptor, @NotNull BiFunction<PlaceholderContext, String, Component> function) {
        registerPlaceholder(descriptor, (ctx, args) -> {
            Component resultText = function.apply(ctx, args);
            return PlaceholderResult.value(resultText);
        });
    }

    @SuppressWarnings("resource")
    public static void registerServerPlaceholder(@NotNull PlaceholderDescriptor descriptor, @NotNull BiFunction<MinecraftServer, String, Component> function) {

        registerPlaceholder(descriptor, (ctx, args) -> {
            // NOTE: The `args` should be verified by the placeholder itself.
            if (ctx.server() == null) {
                return PlaceholderResult.invalid(PlaceholderHelper.NO_SERVER_ERROR_REASON);
            }
            Component resultText = function.apply(ctx.server(), args);
            return PlaceholderResult.value(resultText);
        });
    }

    public static void registerPlayerPlaceholder(@NotNull PlaceholderDescriptor descriptor, @NotNull BiFunction<ServerPlayer, String, Component> function) {

        registerPlaceholder(descriptor, (ctx, args) -> {
            if (ctx.player() == null) {
                return PlaceholderResult.invalid(NO_PLAYER_ERROR_REASON);
            }

            ServerPlayer serverPlayer;
            #if MC_VER < MC_26_1
            serverPlayer = ctx.player();
            #elif MC_VER >= MC_26_1
            serverPlayer = ctx.serverPlayer();
            #endif

            Component resultText = function.apply(serverPlayer, args);
            return PlaceholderResult.value(resultText);
        });
    }

    public static void registerServerPlaceholder(@NotNull PlaceholderDescriptor descriptor, @NotNull Function<MinecraftServer, Component> function) {
        registerServerPlaceholder(descriptor, (server, args) -> function.apply(server));
    }

    public static void registerPlayerPlaceholder(@NotNull PlaceholderDescriptor descriptor, @NotNull Function<ServerPlayer, Component> function) {
        registerPlayerPlaceholder(descriptor, (player, args) -> function.apply(player));
    }

    public static @NotNull String parsePlaceholderString(@NotNull ServerPlayer player, @NotNull String placeholderString) {
        Component text = TextHelper.getTextByValue(player, placeholderString);
        placeholderString = TextHelper.Operators.getString(text);
        return placeholderString;
    }

}

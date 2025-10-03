package mod.fuji.core.auxiliary.minecraft;

import eu.pb4.placeholders.api.PlaceholderHandler;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import mod.fuji.Fuji;
import mod.fuji.core.document.descriptor.PlaceholderDescriptor;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;
import java.util.function.Function;
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

    public static Text makeInvalidArgsErrorText() {
        return PlaceholderResult.invalid(INVALID_ARGS_ERROR_REASON).text();
    }

    @SuppressWarnings("resource")
    public static void registerServerPlaceholder(@NotNull PlaceholderDescriptor descriptor, @NotNull BiFunction<MinecraftServer, String, Text> function) {
        PlaceholderHandler placeholderHandler = (ctx, args) -> {
            // NOTE: The `args` should be verified by the placeholder itself.
            if (ctx.server() == null) {
                return PlaceholderResult.invalid(PlaceholderHelper.NO_SERVER_ERROR_REASON);
            }
            Text resultText = function.apply(ctx.server(), args);
            return PlaceholderResult.value(resultText);
        };

        String placeholderName = descriptor.getString();
        Placeholders.register(Identifier.of(Fuji.MOD_ID, placeholderName), placeholderHandler);
    }

    public static void registerPlayerPlaceholder(@NotNull PlaceholderDescriptor descriptor, @NotNull BiFunction<ServerPlayerEntity, String, Text> function) {
        PlaceholderHandler placeholderHandler = (ctx, args) -> {
            if (ctx.player() == null) {
                return PlaceholderResult.invalid(NO_PLAYER_ERROR_REASON);
            }
            Text resultText = function.apply(ctx.player(), args);
            return PlaceholderResult.value(resultText);
        };

        String placeholderName = descriptor.getString();
        Placeholders.register(Identifier.of(Fuji.MOD_ID, placeholderName), placeholderHandler);
    }

    public static void registerServerPlaceholder(@NotNull PlaceholderDescriptor descriptor, @NotNull Function<MinecraftServer, Text> function) {
        registerServerPlaceholder(descriptor, (server, args) -> function.apply(server));
    }

    public static void registerPlayerPlaceholder(@NotNull PlaceholderDescriptor descriptor, @NotNull Function<ServerPlayerEntity, Text> function) {
        registerPlayerPlaceholder(descriptor, (player, args) -> function.apply(player));
    }

}

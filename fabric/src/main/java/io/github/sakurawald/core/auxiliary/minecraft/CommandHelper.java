package io.github.sakurawald.core.auxiliary.minecraft;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import lombok.experimental.UtilityClass;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@UtilityClass
public class CommandHelper {

    public static final String UUID = "uuid";
    public static final int EXCEPTION_COLOR = 16736000;

    public static @NotNull String computeCommandNodePath(CommandNode<ServerCommandSource> node) {
        CommandDispatcher<ServerCommandSource> dispatcher = ServerHelper.getCommandDispatcher();
        assert dispatcher != null;
        String[] array = dispatcher.getPath(node).toArray(new String[]{});
        return String.join(".", array);
    }

    public static void updateCommandTree() {
        CommandManager commandManager = ServerHelper.getServer().getCommandManager();
        ServerHelper.getPlayers().forEach(commandManager::sendCommandTree);
    }

    public static List<CommandNode<ServerCommandSource>> getCommandNodes() {
        List<CommandNode<ServerCommandSource>> ret = new ArrayList<>();
        RootCommandNode<ServerCommandSource> root = Objects.requireNonNull(ServerHelper.getCommandDispatcher()).getRoot();
        getCommandNodes(ret, root);
        return ret;
    }

    private static void getCommandNodes(List<CommandNode<ServerCommandSource>> collector, CommandNode<ServerCommandSource> parent) {
        parent.getChildren().forEach(it -> getCommandNodes(collector, it));

        // ignore the root command node
        if (!parent.getName().isEmpty()) {
            collector.add(parent);
        }
    }

    @SuppressWarnings("unused")
    public static class Return {
        public static final int FAIL = -1;
        public static final int PASS = 0;
        public static final int SUCCESS = 1;

        private static int fromBoolean(boolean value) {
            return value ? SUCCESS : FAIL;
        }

        public static int outputBoolean(ServerCommandSource source, boolean value) {
            // only send the message feedback to player, to avoid the console spam
            if (source.isExecutedByPlayer()) {
                source.sendMessage(Text.literal(String.valueOf(value)));
            }
            return fromBoolean(value);
        }
    }

    public static class Suggestion {
        public static <T> @NotNull SuggestionProvider<ServerCommandSource> enums(Supplier<T[]> enumSupplier) {
            return (context, builder) -> {
                for (T value : enumSupplier.get()) {
                    builder.suggest(value.toString());
                }
                return builder.buildFuture();
            };
        }

        public static <T> @NotNull SuggestionProvider<ServerCommandSource> iterable(Supplier<Iterable<T>> iterableSupplier) {
            return (context, builder) -> {
                for (T value : iterableSupplier.get()) {
                    builder.suggest(value.toString());
                }
                return builder.buildFuture();
            };
        }

        public static <T> @NotNull SuggestionProvider<ServerCommandSource> identifiers(RegistryKey<? extends Registry<T>> registryKey) {
            return iterable(() -> RegistryHelper
                .ofRegistry(registryKey)
                .getIds());
        }
    }

    public static class Pattern {

        public static int playerOnlyCommand(@NotNull CommandContext<ServerCommandSource> ctx, @NotNull Function<ServerPlayerEntity, Integer> function) {
            ServerPlayerEntity player = ctx.getSource().getPlayer();
            if (player == null) {
                TextHelper.sendMessageByKey(ctx.getSource(), "command.player_only");
                return Return.SUCCESS;
            }

            return function.apply(player);
        }

        public static int itemInHandCommand(@NotNull CommandContext<ServerCommandSource> ctx, @NotNull BiFunction<ServerPlayerEntity, ItemStack, Integer> consumer) {
            return playerOnlyCommand(ctx, player -> {
                ItemStack mainHandStack = player.getMainHandStack();
                if (mainHandStack.isEmpty()) {
                    TextHelper.sendMessageByKey(player, "item.empty.not_allow");
                    return Return.FAIL;
                }
                return consumer.apply(player, mainHandStack);
            });
        }
    }

}

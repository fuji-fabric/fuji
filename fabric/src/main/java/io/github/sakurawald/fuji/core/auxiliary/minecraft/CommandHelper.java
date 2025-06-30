package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

#if MC_VER > MC_1_21
import net.minecraft.server.world.ServerWorld;
#endif

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CommandHelper {

    public static final String UUID_ARGUMENT_NAME = "uuid";
    public static final int COMMAND_EXCEPTION_COLOR = 16736000;

    public static @NotNull String computeCommandNodePath(@NotNull CommandNode<ServerCommandSource> node) {
        CommandDispatcher<ServerCommandSource> dispatcher = ServerHelper.getCommandDispatcher();
        assert dispatcher != null;

        // Find the first encountered path in root tree, ignore other paths if there are `forks` or `redirects`.
        String[] array = dispatcher
            .getPath(node)
            .toArray(new String[]{});
        return String.join(".", array);
    }

    public static String trimPathString(String path) {
        return StringUtils.strip(path, ".");
    }

    public static @NotNull String computeCommandNodePath(List<ParsedCommandNode<ServerCommandSource>> nodes) {
        return nodes.stream()
            .map(it -> it.getNode().getName())
            .collect(Collectors.joining("."));
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
        parent.getChildren()
            .forEach(it -> getCommandNodes(collector, it));

        // Exclude the `root command node` from the result.
        if (!parent.getName().isEmpty()) {
            collector.add(parent);
        }
    }

    public static void ensureItemInHandNotEmpty(ServerPlayerEntity player, ItemStack stack) {
        if (stack.isEmpty()) {
            TextHelper.sendMessageByKey(player, "item.empty.not_allow");
            throw new AbortCommandExecutionException();
        }
    }

    public static @NotNull List<String> getCommandPathPrefixes(List<ParsedCommandNode<ServerCommandSource>> nodes) {
        List<String> commandPaths = new ArrayList<>();
        String rootPath = "";
        for (ParsedCommandNode<ServerCommandSource> node : nodes) {
            rootPath = rootPath + "." + node.getNode().getName();
            rootPath = trimPathString(rootPath);
            commandPaths.add(rootPath);
        }
        return commandPaths;
    }

    public static boolean canUseThisCommand(ServerPlayerEntity player, String commandString) {
        /* Parse the command string into command nodes. */
        ParseResults<ServerCommandSource> parseResults = ServerHelper
            .getCommandDispatcher()
            .parse(commandString, player.getCommandSource());
        CommandContextBuilder<ServerCommandSource> context = parseResults.getContext();

        /* If any exceptions, refuse to use the command. */
        if (!parseResults.getExceptions().isEmpty()) {
            return false;
        }

        /* If the nodes from parsed result is empty, refuse to use the command. */
        List<ParsedCommandNode<ServerCommandSource>> nodes = context.getNodes();
        if (nodes.isEmpty()) return false;

        /* Check the requirement from root to leaf. */
        return nodes
            .stream()
            .map(ParsedCommandNode::getNode)
            .allMatch(it -> it.canUse(player.getCommandSource()));
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

    public static ServerCommandSource getCommandSource(Entity entity) {
        #if MC_VER <= MC_1_21
            return entity.getCommandSource();
        #elif MC_VER > MC_1_21
            return entity.getCommandSource((ServerWorld) entity.getWorld());
        #endif
    }

    public static <S> boolean isExecutedOnServerSide(CommandContextBuilder<S> context) {
        // NOTE: in client-side, the S is not guarantee to be ServerCommandSource.
        return context.getSource() instanceof ServerCommandSource;
    }

    public static String getCommandNodeType(CommandNode<ServerCommandSource> node) {
        if (node instanceof LiteralCommandNode<ServerCommandSource>) return "LiteralCommandNode";
        if (node instanceof ArgumentCommandNode<?,?>) return "ArgumentCommandNode";
        if (node instanceof RootCommandNode<ServerCommandSource>) return "RootCommandNode";

        return "Unknown";
    }

}

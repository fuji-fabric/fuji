package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.github.sakurawald.fuji.core.command.processor.CommandAnnotationProcessor;
import io.github.sakurawald.fuji.core.command.suggestion.CommandSuggestionOptimizer;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class CommandHelper {

    public static final int COMMAND_EXCEPTION_COLOR_INT = 16736000;

    public static class Node {

        public static @NotNull String findCommandNodePath(@NotNull CommandNode<ServerCommandSource> node) {
            CommandDispatcher<ServerCommandSource> dispatcher = getCommandDispatcher();

            /* Find the first encountered path in root tree, ignore other paths if there are `forks` or `redirects`. */
            String[] array = dispatcher
                .getPath(node)
                .toArray(new String[]{});
            return String.join(".", array);
        }

        public static @NotNull String trimCommandPathString(@NotNull String path) {
            return StringUtils.strip(path, ".");
        }

        public static @NotNull String joinCommandNodePath(@NotNull List<ParsedCommandNode<ServerCommandSource>> nodes) {
            // Compute the `command node path` from the only one possible path.
            return nodes
                .stream()
                .map(it -> it.getNode().getName())
                .collect(Collectors.joining("."));
        }

        public static List<CommandNode<ServerCommandSource>> getAllCommandNodes() {
            List<CommandNode<ServerCommandSource>> result = new ArrayList<>();
            CommandDispatcher<ServerCommandSource> commandDispatcher = getCommandDispatcher();
            assert commandDispatcher != null;
            RootCommandNode<ServerCommandSource> root = commandDispatcher.getRoot();
            collectCommandNodes(result, root);
            return result;
        }

        private static void collectCommandNodes(@NotNull List<CommandNode<ServerCommandSource>> collector, @NotNull CommandNode<ServerCommandSource> parent) {
            /* Walk down and collect. */
            parent
                .getChildren()
                .forEach(it -> collectCommandNodes(collector, it));

            if (isRootCommandNode(parent)) {
                collector.add(parent);
            }
        }

        private static boolean isRootCommandNode(@NotNull CommandNode<ServerCommandSource> node) {
            return !node.getName().isEmpty();
        }

        public static @NotNull List<String> getPrefixesOfCommandPath(@NotNull List<ParsedCommandNode<ServerCommandSource>> nodes) {
            List<String> prefixes = new ArrayList<>();

            String walkingPath = "";
            for (ParsedCommandNode<ServerCommandSource> node : nodes) {
                String currentNodeName = node.getNode().getName();
                walkingPath = walkingPath + "." + currentNodeName;
                walkingPath = trimCommandPathString(walkingPath);
                prefixes.add(walkingPath);
            }
            return prefixes;
        }

        @SuppressWarnings("IfCanBeSwitch")
        public static String getCommandNodeType(@NotNull CommandNode<ServerCommandSource> node) {
            if (node instanceof LiteralCommandNode<ServerCommandSource>) return "LiteralCommandNode";
            if (node instanceof ArgumentCommandNode<?, ?>) return "ArgumentCommandNode";
            if (node instanceof RootCommandNode<ServerCommandSource>) return "RootCommandNode";

            return "Unknown";
        }

        public static Optional<CommandNode<ServerCommandSource>> findCommandNode(@NotNull String commandPath) {
            List<String> splitCommandPath = splitCommandPath(commandPath);
            return Optional.ofNullable(getCommandDispatcher()
                .findNode(splitCommandPath));
        }

        private static @NotNull List<String> splitCommandPath(@NotNull String commandPath) {
            String[] nodeNames = commandPath.split("\\.", -1);
            List<String> list = Arrays.asList(nodeNames);
            return list;
        }
    }

    public static void updateCommandTree() {
        updateCommandTree(ServerHelper.getServer().getCommandManager());
    }

    public static void updateCommandTree(@NotNull CommandManager commandManager) {
        // NOTE: No need to update if the command manager is not initialized.
        if (ServerHelper.getServer() == null) {
            return;
        }
        PlayerHelper.Lookup
            .getOnlinePlayers()
            .forEach(commandManager::sendCommandTree);
    }

    public static @NotNull CommandDispatcher<ServerCommandSource> getCommandDispatcher() {
        return CommandAnnotationProcessor.COMMAND_DISPATCHER;
    }

    public static @NotNull CommandRegistryAccess getCommandRegistryAccess() {
        return CommandAnnotationProcessor.COMMAND_REGISTRY_ACCESS;
    }

    public static class Requirement {

        public static boolean canUseThisCommand(ServerPlayerEntity player, String commandString) {
            /* Parse the command string into command nodes. */
            ServerCommandSource commandSource = player.getCommandSource();
            ParseResults<ServerCommandSource> parseResults = getCommandDispatcher()
                .parse(commandString, commandSource);
            CommandContextBuilder<ServerCommandSource> context = parseResults.getContext();

            /* If any exceptions, refuse to use that command. */
            if (!parseResults.getExceptions().isEmpty()) {
                return false;
            }

            /* If the nodes from parsed result is empty, refuse to use that command. */
            List<ParsedCommandNode<ServerCommandSource>> nodes = context.getNodes();
            if (nodes.isEmpty()) return false;

            /* Check the requirement from root to leaf. */
            return nodes
                .stream()
                .map(ParsedCommandNode::getNode)
                .allMatch(it -> it.canUse(commandSource));
        }

        public static boolean isOperator(@NotNull PlayerEntity player) {
            return ServerHelper
                .getServer()
                .getPlayerManager()
                .isOperator(player.getGameProfile());
        }

        @ForDeveloper("""
            By default, an `operator` has the permission level `4`.
            However, it can be configured via `op-permission-level=4` option.
            """)
        public static boolean isAdmin(@NotNull ServerCommandSource source) {
            return source.hasPermissionLevel(4);
        }
    }

    public static class Source {

        public static @NotNull ServerCommandSource getConsoleCommandSource() {
            return ServerHelper.getServer().getCommandSource();
        }

        public static @NotNull ServerCommandSource getCommandSource(@NotNull Entity entity) {
            #if MC_VER <= MC_1_21
            return entity.getCommandSource();
            #elif MC_VER > MC_1_21
            return entity.getCommandSource((net.minecraft.server.world.ServerWorld) entity.getWorld());
            #endif
        }

        public static void withServerPlayerEntity(@NotNull CommandContext<?> context, @NotNull Consumer<ServerPlayerEntity> consumer) {
            Object source = context.getSource();
            if (source instanceof ServerCommandSource serverCommandSource) {
                source = serverCommandSource.getPlayer();
            }

            if (source instanceof ServerPlayerEntity serverPlayerEntity) {
                consumer.accept(serverPlayerEntity);
            }
        }

        @ForDeveloper("""
            If your mod is installed on the client-side, and run the single-player world.
            Then the injected methods in brigadier will be called twice.
            One for ClientCommandSource, one for ServerCommandSource.
            """)
        public static void withServerCommandSource(@NotNull Object indicator, @NotNull Consumer<ServerCommandSource> consumer) {
            indicator = extractCommandSource(indicator);
            if (isServerCommandSource(indicator)) {
                @NotNull ServerCommandSource serverCommandSource = (ServerCommandSource) indicator;
                consumer.accept(serverCommandSource);
            }
        }

        public static void withServerCommandSource(@NotNull Object indicator, @NotNull Runnable runnable) {
            withServerCommandSource(indicator, (serverCommandSource) -> runnable.run());
        }

        public static <S> boolean isExecutedOnServerSide(@NotNull CommandContextBuilder<S> context) {
            // NOTE: in client-side, the S is not guarantee to be ServerCommandSource. (Can be ClientCommandSource)
            return isExecutedOnServerSide(context.getSource());
        }

        public static boolean isExecutedOnServerSide(@NotNull Object indicator) {
            indicator = extractCommandSource(indicator);
            return isServerCommandSource(indicator);
        }

        public static boolean isExecutedByConsole(@NotNull CommandContext<ServerCommandSource> commandContext) {
            return commandContext.getSource().getPlayer() == null;
        }

        private static boolean isServerCommandSource(@NotNull Object object) {
            return object instanceof ServerCommandSource;
        }

        private static @NotNull Object extractCommandSource(@NotNull Object object) {
            /* Try extracting command source from command context. */
            if (object instanceof CommandContext<?> commandContext) {
                object = commandContext.getSource();
            }

            /* Try extracting command source from command context builder. (If the context comes from parsed result) */
            if (object instanceof CommandContextBuilder<?> commandContextBuilder) {
                object = commandContextBuilder.getSource();
            }
            return object;
        }
    }

    public static class Return {
        public static final int FAILURE = 0;
        public static final int SUCCESS = 1;

        private static int fromBoolean(boolean value) {
            return value ? SUCCESS : FAILURE;
        }

        public static int returnBoolean(ServerCommandSource source, boolean value) {
            return fromBoolean(value);
        }

        public static boolean isSuccess(int commandReturnValue) {
            return commandReturnValue > 0;
        }
    }

    public static class Suggestion {

        private static <T> @NotNull CompletableFuture<Suggestions> makeSuggestionsCompletableFuture(@NotNull SuggestionsBuilder builder, @NotNull Supplier<Iterable<T>> iterableSupplier) {
            /* Optimize the command suggestion. */
            Iterable<T> iterable = iterableSupplier.get();
            CommandSuggestionOptimizer
                .optimize(iterable, builder.getRemaining())
                .forEach(builder::suggest);

            return builder.buildFuture();
        }

        public static <T> @NotNull SuggestionProvider<ServerCommandSource> iterable(@NotNull BiFunction<CommandContext<ServerCommandSource>, SuggestionsBuilder, Iterable<T>> iterableSupplier) {
            return (context, builder) -> makeSuggestionsCompletableFuture(builder, () -> iterableSupplier.apply(context, builder));
        }

        public static <T> @NotNull SuggestionProvider<ServerCommandSource> iterable(@NotNull Supplier<Iterable<T>> iterableSupplier) {
            return (context, builder) -> makeSuggestionsCompletableFuture(builder, iterableSupplier);
        }

        public static <T> @NotNull SuggestionProvider<ServerCommandSource> enums(@NotNull Supplier<T[]> enumValuesSupplier) {
            return iterable(() -> Arrays.asList(enumValuesSupplier.get()));
        }

        public static <T> @NotNull SuggestionProvider<ServerCommandSource> identifiers(@NotNull RegistryKey<? extends Registry<T>> registryKey) {
            return iterable(() -> RegistryHelper.getRegistry(registryKey).getIds());
        }
    }

    public static class Pattern {

        public static int withContextPlayer(@NotNull ServerCommandSource source, @NotNull Function<ServerPlayerEntity, Integer> function) {
            ServerPlayerEntity player = source.getPlayer();
            if (player == null) {
                TextHelper.sendTextByKey(source, "command.player_only");
                return Return.SUCCESS;
            }

            return function.apply(player);
        }

        public static int withItemInMainHand(@NotNull ServerCommandSource source, @NotNull BiFunction<ServerPlayerEntity, ItemStack, Integer> consumer) {
            return withContextPlayer(source, player -> {
                ItemStack mainHandStack = player.getMainHandStack();
                if (mainHandStack.isEmpty()) {
                    TextHelper.sendTextByKey(player, "item.empty.not_allow");
                    return Return.FAILURE;
                }
                return consumer.apply(player, mainHandStack);
            });
        }

        public static int withCommandConfirmed(ServerPlayerEntity player, Optional<Boolean> confirm, Supplier<Integer> supplier) {
            return withCommandConfirmed(player.getCommandSource(), confirm, supplier);
        }

        @SuppressWarnings({"BooleanMethodIsAlwaysInverted", "UnnecessaryLocalVariable"})
        public static int withCommandConfirmed(ServerCommandSource source, Optional<Boolean> confirm, Supplier<Integer> supplier) {
            boolean confirmed = confirm.orElse(false);
            if (!confirmed) {
                TextHelper.sendTextByKey(source, "operation.confirm.failed");
                return Return.FAILURE;
            }

            int commandReturnValue = supplier.get();
            return commandReturnValue;
        }
    }

    public static class Context {
        public static <T> Optional<T> tryGetArgument(@NotNull CommandContext<?> commandContext, String argumentName, Class<T> clazz) {
            try {
                return Optional.ofNullable(commandContext.getArgument(argumentName, clazz));
            } catch (Exception e) {
                return Optional.empty();
            }
        }

    }
}

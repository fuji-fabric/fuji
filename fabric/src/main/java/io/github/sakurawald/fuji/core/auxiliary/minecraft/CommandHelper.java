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
import java.util.Optional;
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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CommandHelper {

    public static final String UUID_ARGUMENT_NAME = "uuid";
    public static final int COMMAND_EXCEPTION_COLOR = 16736000;

    public static @NotNull String findCommandNodePath(@NotNull CommandNode<ServerCommandSource> node) {
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

    public static @NotNull String joinCommandNodePath(List<ParsedCommandNode<ServerCommandSource>> nodes) {
        // Compute the `command node path` from the only one possible path.
        return nodes
            .stream()
            .map(it -> it.getNode().getName())
            .collect(Collectors.joining("."));
    }

    public static void updateCommandTree() {
        CommandManager commandManager = ServerHelper.getServer().getCommandManager();
        ServerHelper
            .getOnlinePlayers()
            .forEach(commandManager::sendCommandTree);
    }

    public static List<CommandNode<ServerCommandSource>> getCommandNodes() {
        List<CommandNode<ServerCommandSource>> result = new ArrayList<>();
        CommandDispatcher<ServerCommandSource> commandDispatcher = ServerHelper.getCommandDispatcher();
        assert commandDispatcher != null;
        RootCommandNode<ServerCommandSource> root = commandDispatcher.getRoot();
        collectCommandNodes(result, root);
        return result;
    }

    private static void collectCommandNodes(List<CommandNode<ServerCommandSource>> collector, CommandNode<ServerCommandSource> parent) {
        /* Walk down and collect. */
        parent
            .getChildren()
            .forEach(it -> collectCommandNodes(collector, it));

        if (isRootCommandNode(parent)) {
            collector.add(parent);
        }
    }

    private static boolean isRootCommandNode(CommandNode<ServerCommandSource> node) {
        return !node.getName().isEmpty();
    }

    public static @NotNull List<String> getPrefixesOfCommandPath(List<ParsedCommandNode<ServerCommandSource>> nodes) {
        List<String> prefixes = new ArrayList<>();

        String walkingPath = "";
        for (ParsedCommandNode<ServerCommandSource> node : nodes) {
            String currentNodeName = node.getNode().getName();
            walkingPath = walkingPath + "." + currentNodeName;
            walkingPath = trimPathString(walkingPath);
            prefixes.add(walkingPath);
        }
        return prefixes;
    }

    public static boolean canUseThisCommand(ServerPlayerEntity player, String commandString) {
        /* Parse the command string into command nodes. */
        ServerCommandSource commandSource = player.getCommandSource();
        ParseResults<ServerCommandSource> parseResults = ServerHelper
            .getCommandDispatcher()
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

    @SuppressWarnings("unused")
    public static class Return {
        public static final int FAIL = -1;
        public static final int PASS = 0;
        public static final int SUCCESS = 1;

        private static int fromBoolean(boolean value) {
            return value ? SUCCESS : FAIL;
        }

        public static int returnBoolean(ServerCommandSource source, boolean value) {
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
            return iterable(() ->
                RegistryHelper
                    .getRegistry(registryKey)
                    .getIds());
        }
    }

    public static class Pattern {

        public static int playerOnlyCommand(@NotNull ServerCommandSource source, @NotNull Function<ServerPlayerEntity, Integer> function) {
            ServerPlayerEntity player = source.getPlayer();
            if (player == null) {
                TextHelper.sendTextByKey(source, "command.player_only");
                return Return.SUCCESS;
            }

            return function.apply(player);
        }

        public static int itemInHandCommand(@NotNull ServerCommandSource source, @NotNull BiFunction<ServerPlayerEntity, ItemStack, Integer> consumer) {
            return playerOnlyCommand(source, player -> {
                ItemStack mainHandStack = player.getMainHandStack();
                if (mainHandStack.isEmpty()) {
                    TextHelper.sendTextByKey(player, "item.empty.not_allow");
                    return Return.FAIL;
                }
                return consumer.apply(player, mainHandStack);
            });
        }

        @SuppressWarnings({"BooleanMethodIsAlwaysInverted", "UnnecessaryLocalVariable"})
        public static int withCommandConfirmed(ServerCommandSource source, Optional<Boolean> confirm, Supplier<Integer> supplier) {
            boolean confirmed = confirm.orElse(false);
            if (!confirmed) {
                TextHelper.sendTextByKey(source, "operation.confirm.failed");
                return Return.FAIL;
            }

            int commandReturnValue = supplier.get();
            return commandReturnValue;
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
        // NOTE: in client-side, the S is not guarantee to be ServerCommandSource. (Can be ClientCommandSource)
        return context.getSource() instanceof ServerCommandSource;
    }

    public static boolean isExecutedByConsole(CommandContext<ServerCommandSource> commandContext) {
        return commandContext.getSource().getPlayer() == null;
    }

    public static String getCommandNodeType(CommandNode<ServerCommandSource> node) {
        if (node instanceof LiteralCommandNode<ServerCommandSource>) return "LiteralCommandNode";
        if (node instanceof ArgumentCommandNode<?, ?>) return "ArgumentCommandNode";
        if (node instanceof RootCommandNode<ServerCommandSource>) return "RootCommandNode";

        return "Unknown";
    }

}

package mod.fuji.core.auxiliary.minecraft;

import com.mojang.authlib.GameProfile;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import mod.fuji.core.annotation.Unused;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.command.extension.CommandNodeExtension;
import mod.fuji.core.command.processor.CommandAnnotationProcessor;
import mod.fuji.core.command.structure.RegisteredCommandNode;
import mod.fuji.core.command.suggestion.CommandSuggestionOptimizer;
import mod.fuji.core.config.mapper.wrapper.GameProfileWrapper;
import mod.fuji.core.document.annotation.TestCase;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandHelper {

    public static @NotNull CommandDispatcher<CommandSourceStack> getCommandDispatcher() {
        return CommandAnnotationProcessor.COMMAND_DISPATCHER;
    }

    public static @NotNull CommandBuildContext getCommandRegistryAccess() {
        return CommandAnnotationProcessor.COMMAND_REGISTRY_ACCESS;
    }

    public static class Path {

        public static boolean isLinearCommandPath(@NotNull CommandNode<CommandSourceStack> navigationNode) {
            return toLinearCommandPathList(navigationNode)
                .isPresent();
        }

        @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
        public static Optional<List<String>> toLinearCommandPathList(@NotNull CommandNode<CommandSourceStack> navigationNode) {
            List<String> names = new ArrayList<>();
            CommandNode<CommandSourceStack> current = navigationNode;

            while (true) {
                /* Visit. */
                names.add(current.getName());

                /* Go down. */
                List<CommandNode<CommandSourceStack>> children = new ArrayList<>(current.getChildren());
                if (children.isEmpty()) {
                    break;
                } else if (children.size() == 1) {
                    // NOTE: There should only be 1 child.
                    current = children.get(0);
                } else {
                    return Optional.empty();
                }
            }

            return Optional.of(names);
        }

        public static Optional<String> toLinearCommandPathString(@NotNull CommandNode<CommandSourceStack> navigationNode) {
            return toLinearCommandPathList(navigationNode)
                .map(Path::joinCommandPath);
        }

        public static @NotNull String toLinearCommandPathString(@NotNull List<CommandNode<CommandSourceStack>> nodes) {
            // Compute the `command node path` from the only one possible path.
            return nodes
                .stream()
                .map(CommandNode::getName)
                .collect(Collectors.joining("."));
        }

        public static @NotNull String toFlatCommandPathString(@NotNull CommandNode<CommandSourceStack> navigationNode) {
            StringBuilder flatCommandPath = new StringBuilder();
            flatCommandPath.append(navigationNode.getName());

            navigationNode
                // NOTE: Flatten each child, to concat the `optional arguments` in order.
                .getChildren()
                .forEach(child -> flatCommandPath.append(".").append(toFlatCommandPathString(child)));
            return flatCommandPath.toString();
        }

        public static @NotNull String joinCommandPath(@NotNull List<String> nodes) {
            return String.join(".", nodes);
        }

        public static @NotNull String trimCommandPathString(@NotNull String path) {
            return StringUtils.strip(path, ".");
        }

        public static @NotNull List<String> getPrefixesOfCommandPath(@NotNull List<ParsedCommandNode<CommandSourceStack>> nodes) {
            List<String> prefixes = new ArrayList<>();

            String walkingPath = "";
            for (ParsedCommandNode<CommandSourceStack> node : nodes) {
                String currentNodeName = node.getNode().getName();
                walkingPath = walkingPath + "." + currentNodeName;
                walkingPath = trimCommandPathString(walkingPath);
                prefixes.add(walkingPath);
            }
            return prefixes;
        }

        private static @NotNull List<String> splitCommandPath(@NotNull String commandPath) {
            String[] nodeNames = commandPath.split("\\.", -1);
            return Arrays.asList(nodeNames);
        }

    }

    public static class Node {

        private static boolean isRootCommandNode(@NotNull CommandNode<CommandSourceStack> node) {
            return node.getName().isEmpty()
                || node instanceof RootCommandNode<CommandSourceStack>;
        }

        @SuppressWarnings("IfCanBeSwitch")
        public static @NotNull String toCommandNodeTypeString(@NotNull CommandNode<CommandSourceStack> node) {
            if (node instanceof LiteralCommandNode<CommandSourceStack>) return "LiteralCommandNode";
            if (node instanceof ArgumentCommandNode<?, ?>) return "ArgumentCommandNode";
            if (node instanceof RootCommandNode<CommandSourceStack>) return "RootCommandNode";

            return "UnknownType";
        }

        public static boolean isExecutableCommandNode(@NotNull CommandNode<CommandSourceStack> node) {
            return node.getCommand() != null;
        }

        public static boolean isRedirectCommandNode(@NotNull CommandNode<CommandSourceStack> node) {
            return node.getRedirect() != null;
        }

        public static boolean isExecutableOrRedirectCommandNode(@NotNull CommandNode<CommandSourceStack> node) {
            return isExecutableCommandNode(node) || isRedirectCommandNode(node);
        }
    }

    public static class Tree {

        public static @NotNull List<List<RegisteredCommandNode>> findCommandTree(@NotNull CommandNode<CommandSourceStack> navigationNode) {
            /* Set up the primary branch. */
            List<List<RegisteredCommandNode>> treeCollector = new ArrayList<>();
            List<RegisteredCommandNode> branchCollector = new ArrayList<>();
            treeCollector.add(branchCollector);

            /* Find recursively. */
            RootCommandNode<CommandSourceStack> root = getRootCommandNode();
            findCommandTreeRecursively(treeCollector, branchCollector, navigationNode, root);
            return treeCollector;
        }

        /**
         * Returns a chain of registered command nodes.
         **/
        @SuppressWarnings("UnnecessaryLocalVariable")
        private static void findCommandTreeRecursively(@NotNull List<List<RegisteredCommandNode>> treeCollector, @NotNull List<RegisteredCommandNode> branchCollector, @NotNull CommandNode<CommandSourceStack> navigationNode, @NotNull CommandNode<CommandSourceStack> walkingNode) {
            CommandNode<CommandSourceStack> parent = walkingNode;

            Optional
                .ofNullable(parent.getChild(navigationNode.getName()))
                .ifPresent(child -> {
                    /* Walk the path. */
                    RegisteredCommandNode found = new RegisteredCommandNode(parent, child);
                    branchCollector.add(found);

                    /* Go down. */
                    Collection<CommandNode<CommandSourceStack>> children = navigationNode.getChildren();
                    children
                        .forEach(newNavigationNode -> {
                            if (children.size() == 1) {
                                /* Only 1 branch, continuing it. */
                                findCommandTreeRecursively(treeCollector, branchCollector, newNavigationNode, child);
                            } else {
                                /* More than 1 branch, forking it. */
                                ArrayList<RegisteredCommandNode> forkedCollector = new ArrayList<>(branchCollector);
                                treeCollector.add(forkedCollector);
                                findCommandTreeRecursively(treeCollector, forkedCollector, newNavigationNode, child);
                            }
                        });
                });

        }

        public static void removeCommandTree(@NotNull RegisteredCommandNode registeredCommandNode) {
            // NODE: Identify the `command node` by node name.
            @SuppressWarnings("unchecked")
            CommandNodeExtension<CommandSourceStack> parentNode = (CommandNodeExtension<CommandSourceStack>) registeredCommandNode.getParent();
            CommandNode<CommandSourceStack> childNode = registeredCommandNode.getNode();
            parentNode.fuji$getChildren().values().removeIf(it -> it.getName().equals(childNode.getName()));
            parentNode.fuji$getLiterals().values().removeIf(it -> it.getName().equals(childNode.getName()));
            parentNode.fuji$getArguments().values().removeIf(it -> it.getName().equals(childNode.getName()));
        }

        public static void updateCommandTree() {
            @NotNull Commands commandManager = ServerHelper.getServer().getCommands();
            updateCommandTree(commandManager);
        }

        public static void updateCommandTree(@NotNull Commands commandManager) {
            // NOTE: No need to update if the command manager is not initialized.
            ServerHelper.Lifecycle
                .withServerInstantiated(() -> {
                    PlayerHelper.Lookup
                        .getOnlinePlayers()
                        .forEach(commandManager::sendCommands);
                });
        }

        public static RootCommandNode<CommandSourceStack> getRootCommandNode() {
            return getCommandDispatcher().getRoot();
        }

        public static Optional<CommandNode<CommandSourceStack>> findCommandNode(@NotNull List<String> commandNodePath) {
            return Optional.ofNullable(getCommandDispatcher().findNode(commandNodePath));
        }

        public static Optional<CommandNode<CommandSourceStack>> findCommandNode(@NotNull String commandPath) {
            List<String> splitCommandPath = Path.splitCommandPath(commandPath);
            return findCommandNode(splitCommandPath);
        }

        public static @NotNull String findCommandNodePathString(@NotNull CommandNode<CommandSourceStack> leafNode) {
            List<String> nodes = findCommandNodePathList(leafNode);
            return Path.joinCommandPath(nodes);
        }

        private static @NotNull List<String> findCommandNodePathList(@NotNull CommandNode<CommandSourceStack> leafNode) {
            CommandDispatcher<CommandSourceStack> dispatcher = getCommandDispatcher();

            /* Find the first encountered path in root tree, ignore other paths if there are `forks` or `redirects`. */
            return new ArrayList<>(dispatcher.getPath(leafNode));
        }

        public static List<CommandNode<CommandSourceStack>> getAllCommandNodes() {
            List<CommandNode<CommandSourceStack>> result = new ArrayList<>();
            RootCommandNode<CommandSourceStack> root = getCommandDispatcher().getRoot();
            collectCommandNodes(result, root);
            return result;
        }

        private static void collectCommandNodes(@NotNull List<CommandNode<CommandSourceStack>> collector, @NotNull CommandNode<CommandSourceStack> parent) {
            /* Walk down and collect. */
            parent
                .getChildren()
                .forEach(it -> collectCommandNodes(collector, it));

            if (!Node.isRootCommandNode(parent)) {
                collector.add(parent);
            }
        }

        @SuppressWarnings("unchecked")
        public static void replaceChild(final CommandNode<CommandSourceStack> parent, final CommandNode<CommandSourceStack> node) {
            if (node instanceof RootCommandNode) {
                throw new UnsupportedOperationException("Cannot add a RootCommandNode as a child to any other CommandNode");
            }

            CommandNodeExtension<CommandSourceStack> parentExtension = (CommandNodeExtension<CommandSourceStack>) parent;
            var parentChildren = parentExtension.fuji$getChildren();

            final CommandNode<CommandSourceStack> child = parentChildren.get(node.getName());
            if (child != null) {
                // We've found something to merge onto
                CommandNodeExtension<CommandSourceStack> childExtension = (CommandNodeExtension<CommandSourceStack>) child;
                if (node.getCommand() != null) {
                    childExtension.fuji$setCommand(node.getCommand());
                }
                // NOTE: Set redirect, if specified.
                if (node.getRedirect() != null) {
                    childExtension.fuji$setRedirect(node.getRedirect());
                }
                // NOTE: Set requirement, if specified.
                if (node.getRequirement() != null) {
                    childExtension.fuji$setRequirement(node.getRequirement());
                }
                for (final CommandNode<CommandSourceStack> grandchild : node.getChildren()) {
                    replaceChild(child, grandchild);
                }
            } else {
                setMappings(parent, node);
            }
        }

        @SuppressWarnings("unchecked")
        private static void setMappings(@NotNull CommandNode<CommandSourceStack> parent, @NotNull CommandNode<CommandSourceStack> node) {
            /* Get mappings. */
            CommandNodeExtension<CommandSourceStack> parentExtension = (CommandNodeExtension<CommandSourceStack>) parent;
            var parentChildren = parentExtension.fuji$getChildren();
            var parentLiterals = parentExtension.fuji$getLiterals();
            var parentArguments = parentExtension.fuji$getArguments();

            /* Update the mappings. */
            parentChildren.put(node.getName(), node);
            if (node instanceof LiteralCommandNode) {
                parentLiterals.put(node.getName(), (LiteralCommandNode<CommandSourceStack>) node);
            } else if (node instanceof ArgumentCommandNode) {
                parentArguments.put(node.getName(), (ArgumentCommandNode<CommandSourceStack, ?>) node);
            }
        }

        /**
         * Check if the given navigation command will override an existing command path in the server command tree.
         **/
        public static boolean isCommandNodeRegistered(@NotNull CommandNode<CommandSourceStack> navigationNode) {
            if (!Path.isLinearCommandPath(navigationNode)) {
                LogUtil.warn("There are forks in the given command node: {}", Path.toLinearCommandPathList(navigationNode));
                return false;
            }

            CommandNode<CommandSourceStack> rootNode = getRootCommandNode();
            @Nullable CommandNode<CommandSourceStack> walkingNode = rootNode.getChild(navigationNode.getName());
            if (walkingNode == null) {
                return false;
            }

            return isCommandNodeRegisteredRecursively(navigationNode, walkingNode);
        }

        @SuppressWarnings("RedundantIfStatement")
        @TestCase(action = "Test the functionality of command override detection.", targets = {
            "Create the new command `/home tp -> /say` (with redirect) using `command_alias` module, you should see the override warning.",
            "Create the new command `/home tp -> /say` (without redirect) using `command_bundle` module, you should NOT see the override warning.",
            "Create the new command `/workbench -> /say` (not nested) using `command_alias` module, you should see the override warning."
        })
        private static boolean isCommandNodeRegisteredRecursively(@NotNull CommandNode<CommandSourceStack> navigationNode, @Nullable CommandNode<CommandSourceStack> walkingNode) {
            /* Check pre-conditions. */
            if (walkingNode == null) {
                return false;
            }

            /* Walk down. */
            Collection<CommandNode<CommandSourceStack>> navigationNodeChildren = navigationNode.getChildren();
            if (navigationNodeChildren.isEmpty()) {
                /* Case: the length of paths are the same. */
                if (Node.isExecutableOrRedirectCommandNode(walkingNode)) {
                    return true;
                }

                /* Case: the navigation path is shorter than the walking path by 1. */
                if (navigationNode.getRedirect() != null) {
                    return true;
                }

                /* All requirements are met, now let's go up. */
                return false;
            } else {
                boolean treeValue = false;

                // NOTE: The navigation path must be unique, with no forks.
                for (CommandNode<CommandSourceStack> navigationNodeChild : navigationNodeChildren) {
                    @Nullable CommandNode<CommandSourceStack> walkingNodeChild = walkingNode.getChild(navigationNodeChild.getName());
                    boolean branchValue = isCommandNodeRegisteredRecursively(navigationNodeChild, walkingNodeChild);
                    if (branchValue) {
                        /* Pass the true value up. */
                        treeValue = true;
                        return treeValue;
                    }
                }

                return treeValue;
            }
        }
    }

    public static class Requirement {

        public static boolean canUseCommandString(@NotNull ServerPlayer player, @NotNull String commandString) {
            /* Parse the command string into command nodes. */
            CommandSourceStack commandSource = Source.getCommandSource(player);
            ParseResults<CommandSourceStack> parseResults = getCommandDispatcher()
                .parse(commandString, commandSource);
            CommandContextBuilder<CommandSourceStack> context = parseResults.getContext();

            /* If any exceptions, refuse to use that command. */
            if (!parseResults.getExceptions().isEmpty()) {
                return false;
            }

            /* If the nodes from parsed result is empty, refuse to use that command. */
            List<ParsedCommandNode<CommandSourceStack>> nodes = context.getNodes();
            if (nodes.isEmpty()) return false;

            /* Check the requirement from root to leaf. */
            return nodes
                .stream()
                .map(ParsedCommandNode::getNode)
                .allMatch(it -> it.canUse(commandSource));
        }

        public static boolean isOperator(@NotNull Player player) {
            var profile = GameProfileWrapper
                .of(player)
                .toVanillaType()
                .orElseThrow();
            return PlayerHelper.getPlayerManager().isOp(profile);
        }

        public static boolean isAdmin(@NotNull ServerPlayer player) {
            CommandSourceStack commandSource = Source.getCommandSource(player);
            return isAdmin(commandSource);
        }

        /**
         * By default, an `operator` has the permission level `4`.
         * However, it can be configured via `op-permission-level=4` option.
         **/
        public static boolean isAdmin(@NotNull CommandSourceStack source) {
            return hasLevelPermission(source, 4);
        }

        #if MC_VER >= MC_1_21_11
        private static final net.minecraft.server.permissions.Permission LEVEL_PERMISSION_0 = new net.minecraft.server.permissions.Permission.HasCommandLevel(net.minecraft.server.permissions.PermissionLevel.ALL);
        private static final net.minecraft.server.permissions.Permission LEVEL_PERMISSION_1 = new net.minecraft.server.permissions.Permission.HasCommandLevel(net.minecraft.server.permissions.PermissionLevel.MODERATORS);
        private static final net.minecraft.server.permissions.Permission LEVEL_PERMISSION_2 = new net.minecraft.server.permissions.Permission.HasCommandLevel(net.minecraft.server.permissions.PermissionLevel.GAMEMASTERS);
        private static final net.minecraft.server.permissions.Permission LEVEL_PERMISSION_3 = new net.minecraft.server.permissions.Permission.HasCommandLevel(net.minecraft.server.permissions.PermissionLevel.ADMINS);
        private static final net.minecraft.server.permissions.Permission LEVEL_PERMISSION_4 = new net.minecraft.server.permissions.Permission.HasCommandLevel(net.minecraft.server.permissions.PermissionLevel.OWNERS);

        @SuppressWarnings("IfStatementWithIdenticalBranches")
        private static net.minecraft.server.permissions.Permission mapLevelPermission(int levelPermission) {
            if (levelPermission <= 0) return LEVEL_PERMISSION_0;
            if (levelPermission == 1) return LEVEL_PERMISSION_1;
            if (levelPermission == 2) return LEVEL_PERMISSION_2;
            if (levelPermission == 3) return LEVEL_PERMISSION_3;
            if (levelPermission == 4) return LEVEL_PERMISSION_4;
            // The max allowed level permission is 4.
            return LEVEL_PERMISSION_4;
        }

        @SuppressWarnings({"ControlFlowStatementWithoutBraces", "deprecation"})
        public static net.minecraft.server.permissions.LevelBasedPermissionSet mapLevelPermissionSet(int levelPermission) {
            if (levelPermission <= 0) return net.minecraft.server.permissions.LevelBasedPermissionSet.ALL;
            if (levelPermission == 1) return net.minecraft.server.permissions.LevelBasedPermissionSet.MODERATOR;
            if (levelPermission == 2) return net.minecraft.server.permissions.LevelBasedPermissionSet.GAMEMASTER;
            if (levelPermission == 3) return net.minecraft.server.permissions.LevelBasedPermissionSet.ADMIN;
            if (levelPermission == 4) return net.minecraft.server.permissions.LevelBasedPermissionSet.OWNER;
            return net.minecraft.server.permissions.LevelBasedPermissionSet.OWNER;
        }

        #endif


        public static boolean hasLevelPermission(@NotNull ServerPlayer source, int levelPermission) {
            CommandSourceStack commandSource = Source.getCommandSource(source);
            return hasLevelPermission(commandSource, levelPermission);
        }

        public static boolean hasLevelPermission(@NotNull CommandSourceStack source, int levelPermission) {
            if (levelPermission > 4) return false;

            #if MC_VER < MC_1_21_11
            return source.hasPermission(levelPermission);
            #elif MC_VER >= MC_1_21_11
            return source.permissions().hasPermission(mapLevelPermission(levelPermission));
            #endif
        }

        public static int getLevelPermission(@NotNull GameProfile gameProfile) {
            var vanillaType = GameProfileWrapper
                .fromVanillaType(gameProfile)
                .toVanillaType()
                .orElseThrow();

            #if MC_VER < MC_1_21_11
            return ServerHelper.getServer().getProfilePermissions(vanillaType);
            #elif MC_VER >= MC_1_21_11
            return ServerHelper.getServer()
                .getProfilePermissions(vanillaType)
                .level()
                .id();
            #endif
        }

        public static @NotNull CommandSourceStack withPermissionLevel(@NotNull CommandSourceStack source, int levelPermission) {
            #if MC_VER < MC_1_21_11
            return source.withPermission(levelPermission);
            #elif MC_VER >= MC_1_21_11
            return source.withMaximumPermission(mapLevelPermissionSet(levelPermission));
            #endif
        }
    }

    public static class Source {

        public static @NotNull CommandSourceStack getConsoleCommandSource() {
            return ServerHelper.getServer().createCommandSourceStack();
        }

        public static @NotNull CommandSourceStack getCommandSource(@NotNull ServerPlayer player) {
            // NOTE: For Entity#getCommandSource(ServerWorld), the level permission is always 0
            // You should use ServerPlayerEntity#getCommandSource, which uses the proper level permission from the player.
            return player.createCommandSourceStack();
        }

        public static void withServerPlayerEntity(@NotNull CommandContextBuilder<CommandSourceStack> contextBuilder, @NotNull Consumer<ServerPlayer> consumer) {
            @NotNull CommandSourceStack source = contextBuilder.getSource();
            withServerPlayerEntity(source, consumer);
        }

        @SuppressWarnings("UnnecessaryReturnStatement")
        public static void withServerPlayerEntity(@NotNull CommandContext<?> context, @NotNull Consumer<ServerPlayer> consumer) {
            @NotNull Object source = context.getSource();

            /* Filter out the ClientCommandSource. */
            if (source instanceof CommandSourceStack serverCommandSource) {
                withServerPlayerEntity(serverCommandSource, consumer);
                return;
            }
        }

        public static void withServerPlayerEntity(@NotNull CommandSourceStack serverCommandSource, @NotNull Consumer<ServerPlayer> consumer) {
            if (isExecutedByPlayer(serverCommandSource)) {
                consumer.accept(serverCommandSource.getPlayer());
            }
        }

        /**
         * If your mod is installed on the client-side, and run the single-player world.
         * Then the injected methods in brigadier will be called twice.
         * One for ClientCommandSource, one for ServerCommandSource.
         **/
        public static void withServerCommandSource(@NotNull Object indicator, @NotNull Consumer<CommandSourceStack> consumer) {
            indicator = extractCommandSource(indicator);

            if (isServerCommandSource(indicator)) {
                @NotNull CommandSourceStack serverCommandSource = (CommandSourceStack) indicator;
                consumer.accept(serverCommandSource);
            }
        }

        public static void withServerCommandSource(@NotNull Object indicator, @NotNull Runnable runnable) {
            withServerCommandSource(indicator, (serverCommandSource) -> runnable.run());
        }

        public static <S> boolean isExecutedOnServerSide(@NotNull CommandContextBuilder<S> context) {
            // NOTE: in client-side, the S is not guarantee to be ServerCommandSource. (Can be ClientCommandSource)
            S source = context.getSource();
            return isExecutedOnServerSide(source);
        }

        public static boolean isExecutedOnServerSide(@NotNull Object indicator) {
            indicator = extractCommandSource(indicator);
            return isServerCommandSource(indicator);
        }

        public static boolean isExecutedByConsole(@NotNull CommandContext<CommandSourceStack> commandContext) {
            @NotNull CommandSourceStack source = commandContext.getSource();
            return isExecutedByConsole(source);
        }

        public static boolean isExecutedByConsole(@NotNull CommandSourceStack commandSource) {
            return !isExecutedByPlayer(commandSource);
        }


        public static boolean isExecutedByPlayer(@NotNull CommandContext<CommandSourceStack> commandContext) {
            @NotNull CommandSourceStack source = commandContext.getSource();
            return isExecutedByPlayer(source);
        }

        public static boolean isExecutedByPlayer(@NotNull CommandSourceStack commandSource) {
            return commandSource.getPlayer() != null;
        }

        public static boolean isSilent(@NotNull CommandSourceStack commandSource) {
            return commandSource.silent;
        }

        private static boolean isServerCommandSource(@NotNull Object object) {
            return object instanceof CommandSourceStack;
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

        public static int returnBoolean(@Unused CommandSourceStack source, boolean value) {
            return fromBoolean(value);
        }

        public static boolean isSuccess(int commandReturnValue) {
            return commandReturnValue > 0;
        }
    }

    public static class Parser {

        public static @NotNull String stripTrailingButKeepOne(@NotNull String input) {
            String result = input.stripTrailing();
            if (result.length() != input.length()) {
                return result + " ";
            }

            return result;
        }

    }

    public static class Suggestion {

        private static <T> @NotNull CompletableFuture<Suggestions> makeSuggestionsCompletableFuture(@NotNull SuggestionsBuilder builder, @NotNull Supplier<Iterable<T>> iterableSupplier) {
            /* Optimize the command suggestion. */
            Iterable<T> iterable = iterableSupplier.get();
            CommandSuggestionOptimizer
                .optimize(iterable, builder.getRemaining())
                .forEach(builder::suggest);

            /* Build the command suggestion feature. */
            return builder.buildFuture();
        }

        public static <T> @NotNull SuggestionProvider<CommandSourceStack> iterable(@NotNull BiFunction<CommandContext<CommandSourceStack>, SuggestionsBuilder, Iterable<T>> iterableSupplier) {
            return (context, builder) -> makeSuggestionsCompletableFuture(builder, () -> iterableSupplier.apply(context, builder));
        }

        public static <T> @NotNull SuggestionProvider<CommandSourceStack> iterable(@NotNull Supplier<Iterable<T>> iterableSupplier) {
            return (context, builder) -> makeSuggestionsCompletableFuture(builder, iterableSupplier);
        }

        public static <T> @NotNull SuggestionProvider<CommandSourceStack> enums(@NotNull Supplier<T[]> enumValuesSupplier) {
            return iterable(() -> Arrays.asList(enumValuesSupplier.get()));
        }

        public static <T> @NotNull SuggestionProvider<CommandSourceStack> identifiers(@NotNull ResourceKey<? extends Registry<T>> registryKey) {
            return iterable(() -> RegistryHelper.getRegistry(registryKey).keySet());
        }

        public static @NotNull Suggestions listSuggestions(@NotNull CommandSourceStack commandSource, @NotNull String commandString) {
            // NOTE: Be careful with the leading space characters and the trailing space characters.
            CommandDispatcher<CommandSourceStack> commandDispatcher = getCommandDispatcher();
            ParseResults<CommandSourceStack> parse = commandDispatcher.parse(commandString, commandSource);
            CompletableFuture<Suggestions> completionSuggestions = commandDispatcher.getCompletionSuggestions(parse);
            return completionSuggestions.join();
        }
    }

    public static class Pattern {

        public static int withServerPlayerCommand(@NotNull CommandSourceStack source, @NotNull Function<ServerPlayer, Integer> function) {
            ServerPlayer player = source.getPlayer();
            if (player == null) {
                TextHelper.sendTextByKey(source, "command.player_only");
                return Return.SUCCESS;
            }

            return function.apply(player);
        }

        public static int withItemInMainHandCommand(@NotNull ServerPlayer source, @NotNull Function<ItemStack, Integer> consumer) {
            CommandSourceStack commandSource = Source.getCommandSource(source);
            return withItemInMainHandCommand(commandSource, (player, item) -> consumer.apply(item));
        }

        public static int withItemInMainHandCommand(@NotNull CommandSourceStack source, @NotNull BiFunction<ServerPlayer, ItemStack, Integer> consumer) {
            return withServerPlayerCommand(source, player -> {
                ItemStack mainHandStack = player.getMainHandItem();
                if (mainHandStack.isEmpty()) {
                    TextHelper.sendTextByKey(player, "item.empty.not_allow");
                    return Return.FAILURE;
                }
                return consumer.apply(player, mainHandStack);
            });
        }

        public static int withCommandConfirmed(ServerPlayer player, Optional<Boolean> confirm, Supplier<Integer> supplier) {
            return withCommandConfirmed(Source.getCommandSource(player), confirm, supplier);
        }

        @SuppressWarnings({"BooleanMethodIsAlwaysInverted", "UnnecessaryLocalVariable"})
        public static int withCommandConfirmed(CommandSourceStack source, Optional<Boolean> confirm, Supplier<Integer> supplier) {
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

package io.github.sakurawald.module.initializer.world;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.annotation.Cite;
import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.auxiliary.minecraft.WorldUtil;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.argument.wrapper.impl.Dimension;
import io.github.sakurawald.core.command.argument.wrapper.impl.DimensionType;
import io.github.sakurawald.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.world.config.model.WorldConfigModel;
import io.github.sakurawald.module.initializer.world.config.model.WorldDataModel;
import io.github.sakurawald.module.initializer.world.gui.WorldGui;
import io.github.sakurawald.module.initializer.world.structure.DimensionNode;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.RandomSeed;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/*
 * RegistryKeys.DIMENSION_TYPE only returns registered `dimension type`.
 *
 * To list multi dimensions, use `RegistryKeys.DIMENSION`.
 * DimensionArgumentType.dimension() is equals to RegistryKeys.DIMENSION, but the DimensionArgumentType()'s suggestion will not suggest new added dimension types.
 *
 * The `RegistryKeys.WORLD` and `RegistryKeys.DIMENSION` can be cast to each other.
 * public static final RegistryKey<Registry<World>> WORLD = RegistryKeys.of("dimension");
 * public static final RegistryKey<Registry<DimensionOptions>> DIMENSION = RegistryKeys.of("dimension");
 */

@Cite("https://github.com/NucleoidMC/fantasy")
@CommandNode("world")
@CommandRequirement(level = 4)
public class WorldInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<WorldConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, WorldConfigModel.class);

    private static final BaseConfigurationHandler<WorldDataModel> storage = new ObjectConfigurationHandler<>("world.json", WorldDataModel.class);

    private static void checkBlacklist(CommandContext<ServerCommandSource> ctx, String identifier) {
        if (config.model().blacklist.dimension_list.contains(identifier)) {
            TextHelper.sendMessageByKey(ctx.getSource(), "world.dimension.blacklist", identifier);
            throw new AbortCommandExecutionException();
        }
    }

    @CommandNode("tp")
    @Document("Teleport to the spawnpoint of the world.")
    private static int $tp(@CommandSource ServerPlayerEntity player, Dimension dimension) {
        ServerWorld world = dimension.getValue();
        WorldUtil.getWorldDefaultSpawn(world, player).teleport(player);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("list")
    private static int $list(@CommandSource ServerCommandSource source) {

        if (source.isExecutedByPlayer()) {
            List<DimensionNode> entities = storage.model().dimension_list;
            new WorldGui(source.getPlayer(), entities, 0).open();

        } else {
            ServerHelper.getWorlds().forEach(world -> {
                String dimensionType = world.getDimensionEntry().getIdAsString();
                String dimension = String.valueOf(world.getRegistryKey().getValue());
                TextHelper.sendMessageByKey(source, "world.dimension.list.entry", dimension, dimensionType);
            });
        }

        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("create")
    private static int $create(@CommandSource CommandContext<ServerCommandSource> ctx, String name,
                               Optional<Long> seed, DimensionType dimensionType) {

        /* make dimension identifier */
        String FUJI_DIMENSION_NAMESPACE = "fuji";
        Identifier dimensionIdentifier = Identifier.of(FUJI_DIMENSION_NAMESPACE, name);

        /* check exist */
        if (ServerHelper.getWorlds().stream().anyMatch(it -> RegistryHelper.ofString(it).equals(dimensionIdentifier.toString()))) {
            TextHelper.sendMessageByKey(ctx.getSource(), "world.dimension.exist");
            return CommandHelper.Return.FAIL;
        }

        /* make dimension entry */
        long $seed = seed.orElse(RandomSeed.getSeed());
        Identifier dimensionTypeIdentifier = Identifier.of(dimensionType.getValue());
        DimensionNode dimensionNode = new DimensionNode(true, dimensionIdentifier.toString(), dimensionTypeIdentifier.toString(), $seed);
        storage.model().dimension_list.add(dimensionNode);
        storage.writeStorage();

        /* request creation */
        WorldManager.requestToCreateWorld(dimensionNode);
        TextHelper.sendBroadcastByKey("world.dimension.created", dimensionIdentifier);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("delete")
    private static int $delete(@CommandSource CommandContext<ServerCommandSource> ctx, Dimension dimension) {
        /* check blacklist */
        ServerWorld world = dimension.getValue();
        String identifier = RegistryHelper.ofString(world);
        checkBlacklist(ctx, identifier);

        /* request to deletion */
        WorldManager.requestToDeleteWorld(world);

        /* write entry */
        Optional<DimensionNode> first = storage.model().dimension_list.stream().filter(o -> o.getDimension().equals(identifier)).findFirst();
        if (first.isEmpty()) {
            TextHelper.sendMessageByKey(ctx.getSource(), "world.dimension.not_found", identifier);
            return CommandHelper.Return.FAIL;
        }
        storage.model().dimension_list.remove(first.get());
        storage.writeStorage();

        TextHelper.sendBroadcastByKey("world.dimension.deleted", identifier);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("reset")
    @Document("Delete and create the specified world.")
    private static int $reset(@CommandSource CommandContext<ServerCommandSource> ctx, Optional<Boolean> useTheSameSeed, Dimension dimension) {
        // draw seed and save
        ServerWorld world = dimension.getValue();
        String identifier = RegistryHelper.ofString(world);
        checkBlacklist(ctx, identifier);

        Optional<DimensionNode> dimensionEntryOpt = storage.model().dimension_list.stream().filter(o -> o.getDimension().equals(identifier)).findFirst();
        if (dimensionEntryOpt.isEmpty()) {
            TextHelper.sendMessageByKey(ctx.getSource(), "world.dimension.not_found");
            return CommandHelper.Return.FAIL;
        }

        // request the deletion
        WorldManager.requestToDeleteWorld(world);

        // set the new seed
        Boolean $useTheSameSeed = useTheSameSeed.orElse(false);
        long newSeed = $useTheSameSeed ? dimensionEntryOpt.get().getSeed() : RandomSeed.getSeed();
        dimensionEntryOpt.get().setSeed(newSeed);
        storage.writeStorage();

        // request the creation
        WorldManager.requestToCreateWorld(dimensionEntryOpt.get());

        TextHelper.sendBroadcastByKey("world.dimension.reset", identifier);
        return CommandHelper.Return.SUCCESS;
    }

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::loadWorlds);
    }

    private void loadWorlds(@NotNull MinecraftServer server) {
        storage.model().dimension_list.stream()
            .filter(DimensionNode::isEnable)
            .forEach(it -> {
                try {
                    WorldManager.requestToCreateWorld(it);
                    LogUtil.info("load dimension {} into the server done.", it.getDimension());
                } catch (Exception e) {
                    LogUtil.error("failed to load dimension `{}`", it, e);
                }
            });
    }
}

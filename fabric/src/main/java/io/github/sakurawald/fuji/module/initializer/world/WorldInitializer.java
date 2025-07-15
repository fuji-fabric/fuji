package io.github.sakurawald.fuji.module.initializer.world;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.Dimension;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.DimensionType;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.Cite;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.world.config.model.WorldConfigModel;
import io.github.sakurawald.fuji.module.initializer.world.config.model.WorldDataModel;
import io.github.sakurawald.fuji.module.initializer.world.gui.WorldGui;
import io.github.sakurawald.fuji.module.initializer.world.service.WorldService;
import io.github.sakurawald.fuji.module.initializer.world.structure.RuntimeDimensionDescriptor;
import io.github.sakurawald.fuji.module.initializer.world.structure.gamerule.BooleanGameRuleMapAdapter;
import io.github.sakurawald.fuji.module.initializer.world.structure.gamerule.GameRuleStore;
import io.github.sakurawald.fuji.module.initializer.world.structure.gamerule.IntegerGameRuleMapAdapter;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.world.GameRules;
import net.minecraft.world.border.WorldBorder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Cite("https://github.com/NucleoidMC/fantasy")
@Document(id = 1751826605981L, value = """
    Provides a unified world management.
    """)
@ColorBox(id = 1751981919874L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ The definition of `world`, ` dimension` and `dimension type`.
    In early Minecraft, a `world` only contains `1 dimension` (The overworld dimension).
    In modern Minecraft, a `world` can contain `3 or more dimensions`. (The overworld, the end and the nether)

    Each `dimension` has its `dimension type`.
    The `dimension type` defines the `chunk generator`.

    See also: https://minecraft.wiki/w/Dimension_definition
    See also: https://minecraft.wiki/w/Dimension_type

    <green>NOTE: You can just think the `dimension` word is identical to `world`.
    """)
@ColorBox(id = 1752458381916L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ How it works?
    In vanilla Minecraft, there is a variable `worlds` in `server`, used to store all `loaded dimensions`.

    The vanilla Minecraft will `load` the 3 `dimensions` on server startup.
    They are `minecraft:overworld`, `minecraft:the_nether` and `minecraft:the_end`.

    So, what we should do is to `imitate` the actions.
    We make the `dimension` instance at server startup.
    And then we put the `dimension` in the `loaded dimensions` list.
    So it will be recognised by the server.

    NOTE: The dimensions created by fuji is named `runtime dimension`, because they are `created` and `loaded` at runtime.

    ◉ Does it need to store any special data in the `world` folder?
    No, we didn't touch the `world` folder, or put any special data into it.

    What we need is minimal, we need to define `runtime dimension descriptor` in the module folder.
    The `runtime dimension descriptor` should provide these information: `dimension id`, `dimension type id`, `seed`.
    """)
@ColorBox(id = 1752458991398L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ How the `world` module generate the dimension?
    Actually, the `world` module didn't do the `world generation` itself.

    A `dimension` is composed by `chunks` (A `16x16 segment` of the `dimension`)
    The `dimension type` defines the `chunk generator`.

    So, what we do is simple, the `runtime dimension descriptor` needs to specify the `dimension type`.
    And we will use the existing `chunk generator` defined by that `dimension type`.

    ◉ How the `chunk generator` works?
    A `chunk generator` need to `fill blocks` in the `given chunk location`.
    You need to give the `seed` to `the chunk generator`, and it will fill blocks for you.

    If the specified chunk is not `generated`, then the chunk generator will `generated` a new one.
    If the specified chunk is `generated`, the chunk generator will just use the `existed chunk data` in storage.
    """)
@ColorBox(id = 1752460350802L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    ◉ The logic of `/worldborder` command.
    The `/worldborder` command only sets the `World Border` of `minecraft:overworld`.
    But there is a listener to broadcast the new `World Border` to `all` dimensions.

    """)



@ColorBox(id = 1752297520453L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ Advanced World Management and Per-world rules.
    The `world` module provided by fuji is a simple module.
    If you want a more powerful tool, you can try use `WorldManager` mod and `WorldGameRules` mod.

    See:
    1. https://github.com/DrexHD/WorldManager
    2. https://github.com/DrexHD/WorldGameRules
    """)
@ColorBox(id = 1751982071236L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Create an extra `the_nether` dimension
    Issue: `/world create my_nether minecraft:the_nether`

    ◉ Delete the extra dimension
    Issue: `/world delete fuji:my_nether --confirm true`

    ◉ Reset the extra dimension with random seed.
    Issue: `/world reset fuji:my_nether --confirm true`

    ◉ Specify a seed for an extra dimension.
    1. `/world create my_nether --seed 1234567890 minecraft:the_nether`
    2. `/world reset fuji:my_nether --useTheSameSeed true --confirm true`
    """)
@ColorBox(id = 1751982158414L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    ◉ Make a resource world that reset automatically every day.
    You can use `command_scheduler` module, to execute `/world reset` command automatically.
    """)
@ColorBox(id = 1752261661452L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    ◉ The logic of `passed ticks` is per-dimension.
    Each fuji runtime dimension will save its own `time_of_day` (The equivalent to `DayTime` in `level.dat`).

    ◉ The duration of `one day` in Minecraft.
    The `total ticks of one day` is `24000 ticks` or `20 minutes`.
    It is `10 minutes of day time` + `10 minutes of night time`.

    ◉ The saved `passed ticks` and `/time` command.
    The value of `Time` in `level.dat` = `/time query gametime`.
    The value of `DayTime` in `level.dat` = `/time query day` * 24000 + `/time query daytime`.

    NOTE: The `minecraft:overworld`, `minecraft:the_nether` and `minecraft:the_end` shares the same instance of `DayTime`.
    NOTE: The `/time set {day/midnight/night/noon} command directly sets the `DayTime` to the first day.

    ◉ The logic of `/time query ...` command.
    For `/time query daytime` command, it returns the `DayTime % 24000` of `the world of the command source`:
    1. If the `command source` is `a player`, then the `world` is `the world where the player is`.
    2. If the `command source` is `the console`, then the `world` is `minecraft:overworld`.

    ◉ The logic of `/time {set/add} ...` command.
    For command `/time {set/add}`, it operates on `all dimensions` in the server.
    """)
@ColorBox(id = 1752287089199L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    ◉ The `weather system` of the `world`.
    There are 3 types of `weather`: `clear`, `rain` and `thunder`.
    If `clear`, then both `rain` and `thunder` is false.
    If `thunder`, then `rain` is true.

    The `weather system` will be `tick` if:
    1. The `dimension options` of the `world` has `skylight`.
    2. The `gamerule DO_WEATHER_CYCLE` of the `world` is true.

    ◉ The logic of `/weather` command.
    The `/weather` command `only` sets the `weather` of `minecraft:overworld`.

    ◉ Set the weather per-dimension.
    You can modify the weather directly in config file, and issue `/fuji reload` to apply it.
    """)
@ColorBox(id = 1752292508145L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    ◉ The logic of `/gamerule` command.
    The `/gamerule` command `only` operates on `minecraft:overworld` dimension.

    To see the `true info` of `a specified dimension`, you should use `/world info` command.

    ◉ Set the `per-dimension gamerules` using commands.
    You can install the `WorldGameRules` mod to provide such commands.
    See https://github.com/DrexHD/WorldGameRules
    """)
@ColorBox(id = 1752429441664L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    ◉ Does the `runtime dimension` support `datapack`?
    It depends on how the `datapack` interfaces with the `world`.
    Most of datapack should work.
    Anyway, always backup your world data before install a new datapack.
    """)
@ColorBox(id = 1752431019812L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    ◉ The logic of `nether portal` and `ender portal`.
    In vanilla Minecraft, there are only 3 dimensions.
    They are `minecraft:overworld`, `minecraft:the_nether` and `minecraft:the_end`.
    They are `hard coded` dimensions.
    The linkage of `nether portal` and `ender portal` use the `hard coded` dimensions.

    ◉ Can I create `nether portal` in `runtime dimension`?
    No, you can't create any `nether portal` in runtime dimension.

    ◉ Can I create `ender portal` in `runtime dimension`?
    Yes, but the destination dimension is hard-coded, it is always the `minecraft:the_end`.

    The logic of `EnderPortalBlockEntity`:
    1. If the player is now in `minecraft:the_end`, then destination dimension is `minecraft:overworld`.
    2. Else the destination dimension is `minecraft:the_end`.
    """)



@CommandNode("world")
@CommandRequirement(level = 4)
public class WorldInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<WorldConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, WorldConfigModel.class);

    public static final BaseConfigurationHandler<WorldDataModel> storage = new ObjectConfigurationHandler<>("world.json", WorldDataModel.class)
        .setAutoSaveEveryMinute();

    private static void checkBlacklist(ServerCommandSource source, String identifier) {
        /* Should not operate on blacklisted dimensions. */
        if (config.model().blacklist.dimension_list.contains(identifier)) {
            TextHelper.sendTextByKey(source, "world.dimension.blacklist", identifier);
            throw new AbortCommandExecutionException();
        }
    }

    private static void ensureDimensionIdNotExists(ServerCommandSource source, Identifier identifier) {
        if (WorldService.existsDimension(identifier)) {
            TextHelper.sendTextByKey(source, "world.dimension.exist");
            throw new AbortCommandExecutionException();
        }
    }

    @Document(id = 1751826609063L, value = "Teleport to the target dimension with the same coordinate.")
    @CommandNode("tp")
    private static int $tp(@CommandSource ServerPlayerEntity player, Dimension dimension) {
        ServerWorld targetDimension = dimension.getValue();

        GlobalPos
            .of(player)
            .withLevel(RegistryHelper.toString(targetDimension))
            .teleport(player);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("list")
    private static int $list(@CommandSource ServerCommandSource source) {
        if (source.isExecutedByPlayer()) {
            List<RuntimeDimensionDescriptor> entities = storage.model().dimension_list;
            new WorldGui(source.getPlayer(), entities, 0)
                .open();
        } else {
            ServerHelper
                .getWorlds()
                .forEach(world -> {
                    String dimensionType = RegistryHelper.getIdAsString(world.getDimensionEntry());
                    String dimension = String.valueOf(world.getRegistryKey().getValue());
                    TextHelper.sendTextByKey(source, "world.dimension.list.entry", dimension, dimensionType);
                });
        }

        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("create")
    private static int $create(@CommandSource ServerCommandSource source, String name,
                               DimensionType dimensionType, Optional<Long> seed) {

        /* Make identifier for the new dimension. */
        final String FUJI_DIMENSION_NAMESPACE = "fuji";
        Identifier dimensionIdentifier = Identifier.of(FUJI_DIMENSION_NAMESPACE, name);
        ensureDimensionIdNotExists(source, dimensionIdentifier);

        /* Make dimension entry */
        long $seed = seed.orElse(RandomSeed.getSeed());
        Identifier dimensionTypeIdentifier = RegistryHelper.makeIdentifier(dimensionType.getValue());
        RuntimeDimensionDescriptor runtimeDimensionDescriptor = new RuntimeDimensionDescriptor();
        runtimeDimensionDescriptor.dimension = dimensionIdentifier.toString();
        runtimeDimensionDescriptor.dimension_type = dimensionTypeIdentifier.toString();
        runtimeDimensionDescriptor.seed = $seed;
        runtimeDimensionDescriptor.setShouldTickTime(true);
        runtimeDimensionDescriptor.gameRules = GameRuleStore.makeDefault();

        storage.model().dimension_list.add(runtimeDimensionDescriptor);
        storage.writeStorage();

        /* Request to create the dimension. */
        WorldService.requestToCreateDimension(runtimeDimensionDescriptor);
        TextHelper.sendBroadcastByKey("world.dimension.created", dimensionIdentifier);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("delete")
    private static int $delete(@CommandSource ServerCommandSource source, Dimension dimension, Optional<Boolean> confirm) {
        ServerWorld dimensionInstance = dimension.getValue();
        String dimensionId = RegistryHelper.toString(dimensionInstance);
        checkBlacklist(source, dimensionId);

        /* Check the command confirm. */
        if (!CommandHelper.Pattern.isCommandConfirmed(source, confirm)) {
            return CommandHelper.Return.FAIL;
        }

        /* Request to delete. */
        WorldService.requestToDeleteDimension(dimensionInstance);

        /* Remove the node from storage. */
        WorldService.deleteDimensionNode(dimensionId);

        TextHelper.sendBroadcastByKey("world.dimension.deleted", dimensionId);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826611302L, value = "Delete and create the specified world.")
    @CommandNode("reset")
    private static int $reset(@CommandSource ServerCommandSource source, Dimension dimension, Optional<Boolean> useTheSameSeed, Optional<Boolean> confirm) {
        /* Check the command confirm. */
        if (!CommandHelper.Pattern.isCommandConfirmed(source, confirm)) {
            return CommandHelper.Return.FAIL;
        }

        /* Get the original dimension node. */
        ServerWorld dimensionInstance = dimension.getValue();
        String dimensionIdentifier = RegistryHelper.toString(dimensionInstance);
        checkBlacklist(source, dimensionIdentifier);

        Optional<RuntimeDimensionDescriptor> dimensionEntryOpt = WorldService.getDimensionDescriptor(dimensionIdentifier);
        if (dimensionEntryOpt.isEmpty()) {
            TextHelper.sendTextByKey(source, "world.dimension.not_found");
            return CommandHelper.Return.FAIL;
        }
        RuntimeDimensionDescriptor runtimeDimensionDescriptor = dimensionEntryOpt.get();

        /* Delete the dimension instance. */
        WorldService.requestToDeleteDimension(dimensionInstance);

        /* Draw the seed. */
        Boolean $useTheSameSeed = useTheSameSeed.orElse(false);
        runtimeDimensionDescriptor.seed = $useTheSameSeed ? runtimeDimensionDescriptor.seed : RandomSeed.getSeed();
        storage.writeStorage();

        /* Create a new dimension instance. */
        WorldService.requestToCreateDimension(runtimeDimensionDescriptor);

        TextHelper.sendBroadcastByKey("world.dimension.reset", dimensionIdentifier);
        return CommandHelper.Return.SUCCESS;
    }


    @Document(id = 1752248825291L, value = """
        Saves the config of all extra dimensions into the storage.
        """)
    @CommandNode("save-configs")
    @CommandRequirement(level = 4)
    private static int $saveConfigs(@CommandSource ServerCommandSource source) {
        WorldService.saveRuntimeWorldConfigs();
        TextHelper.sendTextByKey(source, "operation.success");
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1752433782557L, value = """
        List the dimensions each player is in.
        """)
    @CommandNode("who")
    @CommandRequirement(level = 4)
    private static int $who(@CommandSource ServerCommandSource source) {
        printGroupedPlayersByDimension(source, null);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1752434557605L, value = """
        List the players in specified dimension.
        """)
    @CommandNode("who")
    @CommandRequirement(level = 4)
    private static int $who(@CommandSource ServerCommandSource source, Dimension dimension) {
        printGroupedPlayersByDimension(source, dimension);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1752435062516L, value = """
        Query which dimension the player is in.
        """)
    @CommandNode("who")
    @CommandRequirement(level = 4)
    private static int $who(@CommandSource ServerCommandSource source, ServerPlayerEntity player) {
        String playerName = PlayerHelper.getPlayerName(player);
        String locationDimensionId = RegistryHelper.toString(player.getWorld());
        TextHelper.sendTextByKey(source, "world.who.player", playerName, locationDimensionId);
        return CommandHelper.Return.SUCCESS;
    }

    private static void printGroupedPlayersByDimension(ServerCommandSource source, @Nullable Dimension specifiedDimension) {
        Map<@NotNull String, List<String>> groupedPlayers = ServerHelper
            .getWorlds()
            .stream()
            .collect(Collectors.toMap(
                RegistryHelper::toString
                , world -> world.getPlayers().stream().map(PlayerHelper::getPlayerName).toList()));

        groupedPlayers
            .entrySet()
            .stream()
            .filter(entry -> {
                if (specifiedDimension == null) return true;
                String filterDimension = RegistryHelper.toString(specifiedDimension.getValue());
                return entry.getKey().equals(filterDimension);
            })
            .forEach(entry -> {
                String dimensionId = entry.getKey();
                List<String> players = entry.getValue();
                TextHelper.sendTextByKey(source, "world.who.dimension", dimensionId, players);
        });
    }

    @Document(id = 1752283075945L, value = """
        List the debug info of specified dimension.
        """)
    @CommandNode("info")
    @CommandRequirement(level = 4)
    private static int $info(@CommandSource ServerCommandSource source, Dimension dimension) {
        ServerWorld dimensionInstance = dimension.getValue();

        /* Make text for basic info. */
        TextHelper.sendTextByKey(source, "dimension.id", RegistryHelper.toString(dimensionInstance));
        TextHelper.sendTextByKey(source, "dimension.difficulty", dimensionInstance.getDifficulty());
        TextHelper.sendTextByKey(source, "dimension.seed", dimensionInstance.getSeed());
        TextHelper.sendTextByKey(source, "dimension.options", dimensionInstance.getDimension());
        TextHelper.sendTextByKey(source, "dimension.properties", dimensionInstance.getLevelProperties());
        TextHelper.sendTextByKey(source, "dimension.chunk_generator", dimensionInstance.getChunkManager().getChunkGenerator());

        /* Make text for dimension border. */
        WorldBorder worldBorder = dimensionInstance.getWorldBorder();
        TextHelper.sendTextByKey(source, "dimension.border");
        TextHelper.sendTextByKey(source, "dimension.border.size", worldBorder.getSize());
        TextHelper.sendTextByKey(source, "dimension.border.size.lerp_target", worldBorder.getSizeLerpTarget());
        TextHelper.sendTextByKey(source, "dimension.border.size.lerp_time", worldBorder.getSizeLerpTime());
        TextHelper.sendTextByKey(source, "dimension.border.center.x", worldBorder.getCenterX());
        TextHelper.sendTextByKey(source, "dimension.border.center.z", worldBorder.getCenterZ());
        TextHelper.sendTextByKey(source, "dimension.border.damage.per_block", worldBorder.getDamagePerBlock());
        TextHelper.sendTextByKey(source, "dimension.border.safe_zone", worldBorder.getSafeZone());
        TextHelper.sendTextByKey(source, "dimension.border.warning.blocks", worldBorder.getWarningBlocks());
        TextHelper.sendTextByKey(source, "dimension.border.warning.time", worldBorder.getWarningTime());

        /* Make text for gamerules. */
        MutableText gameRulesText = TextHelper.getTextByKey(source, "dimension.gamerules").copy();
        Map<String, Object> gameRulesMap = new HashMap<>();
        GameRules gameRules = dimensionInstance.getGameRules();
        gameRules.accept(new GameRules.Visitor() {
            @Override
            public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                String gameRuleName = key.getName();
                T gameRuleValue = gameRules.get(key);
                gameRulesMap.put(gameRuleName, gameRuleValue);
            }
        });
        MutableText gameRulesHoverText = TextHelper.Formatter.formatMap(gameRulesMap);
        gameRulesText
            .fillStyle(Style.EMPTY
                .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(gameRulesHoverText)));
        source.sendMessage(gameRulesText);
        TextHelper.sendTextByKey(source, "prompt.hover.see_it");

        return CommandHelper.Return.SUCCESS;
    }


    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::loadDimensions);
    }

    @Override
    protected void registerGsonTypeAdapter() {
        BaseConfigurationHandler.registerGsonTypeAdapter(Reference2BooleanMap.class, new BooleanGameRuleMapAdapter());
        BaseConfigurationHandler.registerGsonTypeAdapter(Reference2IntMap.class, new IntegerGameRuleMapAdapter());
    }

    private void loadDimensions(@NotNull MinecraftServer server) {
        storage.model().dimension_list
            .stream()
            .filter(RuntimeDimensionDescriptor::isAuto_load_on_server_startup)
            .forEach(it -> {
                try {
                    WorldService.requestToCreateDimension(it);
                    LogUtil.info("Load dimension {} into the server.", it.getDimension());
                } catch (Exception e) {
                    LogUtil.error("Failed to load dimension `{}`", it, e);
                }
            });
    }
}

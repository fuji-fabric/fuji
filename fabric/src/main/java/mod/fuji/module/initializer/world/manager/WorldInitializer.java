package mod.fuji.module.initializer.world.manager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.Dimension;
import mod.fuji.core.command.argument.wrapper.impl.DimensionType;
import mod.fuji.core.command.argument.wrapper.impl.FujiIdentifier;
import mod.fuji.core.command.exception.AbortCommandExecutionException;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.config.mapper.GsonMapper;
import mod.fuji.core.document.annotation.Cite;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.job.JobManager;
import mod.fuji.core.structure.GlobalPos;
import mod.fuji.core.structure.IdentifierIR;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.world.manager.auxiliary.WorldBorderHelper;
import mod.fuji.module.initializer.world.manager.command.argument.wrapper.ChunkGeneratorType;
import mod.fuji.module.initializer.world.manager.command.argument.wrapper.LoadedRuntimeDimensionDescriptor;
import mod.fuji.module.initializer.world.manager.command.argument.wrapper.UnloadedRuntimeDimensionDescriptor;
import mod.fuji.module.initializer.world.manager.command.argument.wrapper.WorldPresetType;
import mod.fuji.module.initializer.world.manager.config.model.WorldConfigModel;
import mod.fuji.module.initializer.world.manager.config.model.WorldDataModel;
import mod.fuji.module.initializer.world.manager.service.WorldService;
import mod.fuji.module.initializer.world.manager.service.structure.DimensionCreationTicket;
import mod.fuji.module.initializer.world.manager.service.structure.DimensionDeletionTicket;
import mod.fuji.module.initializer.world.manager.structure.RuntimeDimensionDescriptor;
import mod.fuji.module.initializer.world.manager.structure.RuntimeDimensionImporter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.border.WorldBorder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Cite("https://github.com/NucleoidMC/fantasy")
@Document(id = 1751826605981L, value = """
    Provides a unified world management.
    """)
@ColorBox(id = 1751981919874L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ The definition of `world`, ` dimension` and `dimension type`.
    In early Minecraft, a `world` only contains `1 dimension` (The overworld dimension).
    In modern Minecraft, a `world` can contain `3 or more dimensions`. (The overworld, the end and the nether)

    Each `dimension` has its `dimension type`.
    We can create extra dimensions using existed `dimension type`.

    See also: https://minecraft.wiki/w/Dimension_definition
    See also: https://minecraft.wiki/w/Dimension_type

    <green>NOTE: You can just think the `dimension` word is identical to `world`.
    """)
@ColorBox(id = 1752458381916L, color = ColorBox.ColorBoxTypes.NOTE, value = """
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
    The `runtime dimension descriptor` should provide enough information to define a `Dimension Options`.

    ◉ What is `DimensionOptions`?
    A `DimensionOptions` = A `Dimension Type` + A `Chunk Generator`.
    The `dimension type` is used to define the `environment` of a `dimension`. (Like, `bed explosion?` or `infinite burning?`)
    The `chunk generator` is used to `generate` the `chunks`. (Give the `seed` and `chunk location` to the generator, it will fill blocks for you)

    See also:
    1. https://minecraft.wiki/w/Dimension_type
    2. https://minecraft.wiki/w/Dimension_definition
    3. https://minecraft.wiki/w/Noise_settings
    """)
@ColorBox(id = 1752458991398L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How the `world` module generate the dimension?
    Actually, the `world` module didn't do the `world generation` itself.

    What we do is simple, the `runtime dimension descriptor` provides enough information to create the `DimensionOptions`.
    Note that `Dimension Options = Dimension Type + Chunk Generator`:
    1. We use `Dimension Type` to describe the `environment` of a specific `dimension`. (For example, `bed explosion?`, `infinite burning?`...)
    2. We use `Chunk Generator` to describe how the `chunks` are generated. (We will give the specified `seed` argument to it)

    ◉ What is a `chunk generator`?
    Give a `seed` and `chunk location` to a `chunk generator`.
    The `chunk generator` will `fill` blocks for you.

    There are 2 types of `chunk generator`:
    1. `Flat Chunk Generator`: It is used for `superflat` world, it fills blocks `layer` by `layer`.
    2. `Noise Chunk Generator`: Compared to `Flat Chunk Generator`, it makes some `noise`, so you dimension looks not boring like `superflat dimension`.

    <green>NOTE: The `minecraft:overworld`, `minecraft:the_nether` and `minecraft:the_end` all use `Noise Chunk Generator`, but they have different `Chunk Generator Settings`.
    <green>There are 3 `NoiseChunkGenerator` instances with different `Chunk Generator Settings`.
    <green>That's why the `minecraft:overworld`, `minecraft:the_nether` and `minecraft:the_end` look different.

    ◉ The logic of `chunk generator`.
    If the specified chunk is not `generated`, then the chunk generator will `generate` a new one.
    If the specified chunk is `generated`, the chunk generator will just use the `existed chunk data` in storage.
    """)
@ColorBox(id = 1752297520453L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ Advanced World Management and Per-world rules.
    The `world` module provided by fuji is a simple module.
    If you want a more powerful tool, you can try use `WorldManager` mod and `WorldGameRules` mod.

    See:
    1. https://github.com/DrexHD/WorldManager
    2. https://github.com/DrexHD/WorldGameRules
    """)
@ColorBox(id = 1752788919780L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ Useful concepts about a `dimension`.
    - https://minecraft.wiki/w/World_generation
    - https://minecraft.wiki/w/World_type
    - https://minecraft.wiki/w/World
    - https://minecraft.wiki/w/Biome
    - https://minecraft.wiki/w/Void
    - https://minecraft.wiki/w/Overworld
    - https://minecraft.wiki/w/Feature
    - https://minecraft.wiki/w/Structure
    - https://minecraft.wiki/w/Terrain_features
    - https://minecraft.fandom.com/wiki/Java_Edition_level_format
    - https://minecraft.wiki/w/Chunk_format
    - https://minecraft.wiki/w/Loot_table
    """)
@ColorBox(id = 1751982071236L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Create an extra `the_nether` dimension
    Issue: `/world create my_nether minecraft:the_nether`

    <yellow>NOTE: By default, the dimension namespace is `fuji`.
    <yellow>Issue `/world create "custom_namespace:my_nether" minecraft:the_nether` to use a custom namespace.

    ◉ Delete the extra dimension
    Issue: `/world delete fuji:my_nether --confirm true`

    ◉ Reset the extra dimension with random seed.
    Issue: `/world reset fuji:my_nether --confirm true`

    ◉ Specify a seed for an extra dimension.
    1. `/world create my_nether --seed 1234567890 minecraft:the_nether`
    2. `/world reset fuji:my_nether --useTheSameSeed true --confirm true`
    """)
@ColorBox(id = 1751982158414L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Make a resource world that reset automatically every day.
    You can use `command_scheduler` module, to execute `/world reset` command automatically.
    """)
@ColorBox(id = 1752261661452L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ The logic of `passed ticks` is per-dimension.
    Each fuji runtime dimension will save its own `time_of_day` (The equivalent to `DayTime` in `level.dat`).

    ◉ The duration of `one day` in Minecraft.
    The `total ticks of one day` is `24000 ticks` or `20 minutes`.
    It is `10 minutes of day time` + `10 minutes of night time`.

    ◉ The saved `passed ticks` and `/time` command.
    The value of `Time` in `level.dat` = `/time query gametime`.
    The value of `DayTime` in `level.dat` = `/time query day` * 24000 + `/time query daytime`.

    NOTE: The `minecraft:overworld`, `minecraft:the_nether` and `minecraft:the_end` shares the same instance of `DayTime`.
    NOTE: The `/time set {day/midnight/night/noon}` command directly sets the `DayTime` to the first day.

    ◉ The logic of `/time query ...` command.
    For `/time query daytime` command, it returns the `DayTime % 24000` of `the world of the command source`:
    1. If the `command source` is `a player`, then the `world` is `the world where the player is`.
    2. If the `command source` is `the console`, then the `world` is `minecraft:overworld`.

    ◉ The logic of `/time {set/add} ...` command.
    For command `/time {set/add}`, it operates on `all dimensions` in the server.
    """)
@ColorBox(id = 1752287089199L, color = ColorBox.ColorBoxTypes.TIP, value = """
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
@ColorBox(id = 1752429441664L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Does the `runtime dimension` support `datapack`?
    It depends on how the `datapack` interfaces with the `world`.
    Most of datapack should work.
    Anyway, always backup your world data before install a new datapack.
    """)
@ColorBox(id = 1752431019812L, color = ColorBox.ColorBoxTypes.TIP, value = """
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
@ColorBox(id = 1752733447050L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Create a `flat dimension` with `overworld` dimension type.
    Issue: `/world create my_flat_world minecraft:overworld --chunkGeneratorType FLAT`

    ◉ Create a `flat dimension` with `overworld` dimension type and `customized preset`.
    Issue: `/world create my_desert_world minecraft:overworld --chunkGeneratorType FLAT --chunkGeneratorParameters "minecraft:bedrock,3*minecraft:stone,116*minecraft:sandstone;minecraft:desert"`

    ◉ Create a `void dimension`.
    Issue: `/world create my_void_world minecraft:overworld --chunkGeneratorType FLAT --chunkGeneratorParameters "minecraft:air;minecraft:the_void"`
    <green>NOTE: The `secret` is, a `void dimension` is just a `flat dimension` with customized `minecraft:air layers` with the `minecraft:the_void` biome.

    ◉ Generate the `parameters` using a `generator`.
    See: https://minecraft.tools/en/flat.php

    ◉ Useful resource
    The definition of `world preset`: https://minecraft.fandom.com/wiki/World_preset
    The definition of `superflat dimension`: https://minecraft.fandom.com/wiki/Superflat
    """)
@ColorBox(id = 1752741022214L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Create a `runtime dimension` using pre-defined `world preset`.
    Issue: `/world create example minecraft:overworld --worldPresetType DEBUG_ALL_BLOCK_STATES`

    NOTE: When you are using a pre-defined `world preset`, then the following options will be `ignored`:
    1. `dimension type id`
    2. `chunk generator type`
    3. `chunk generator parameters`.
    """)
@ColorBox(id = 1752807649896L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ The definition of `import`, `make`, `load`, `unload`.
    The `import` means: define a `runtime dimension descriptor` in config file.
    So that we know how make the `original chunk generator` used by the dimension.

    The `make` means: We use the `runtime dimension descriptor` to create the `dimension` instance.
    We need to make the `DimensionOptions` from the `runtime dimension descriptor`.
    A `DimensionOptions` = A `Dimension Type` + A `Chunk Generator`.

    The `load` means: We will put this `dimension` instance into the worlds list of the `server`.

    The `unload` means: We will take this `dimension` instance from the worlds list of the `server`.

    ◉ The definition of `/world create` and `/world delete` command.
    The `/world create` command does the `import`, `make` and `load`.
    The `/world delete` command does the `unload` and also `delete the chunk files of that dimension`.
    """)
@ColorBox(id = 1752811309081L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ How to `import` a `dimension dir`?
    The `/world import` is similar to `/world create` command.
    You need to specify `enough information` to define the `runtime dimension descriptor`.
    Note that `to import a dimension dir` is `to define the runtime dimension descriptor in the config file`.
    <red>NOTE: If you specify a `wrong seed`, then the `chunk generator` will generate `in-consistent new chunks` compared to your `existing chunks`.

    ◉ Import a `sky block dimension`.
    Let's say you have a `sky block dimension` whose directory name is `sky_block`.
    You need to put that `directory` in `world/dimensions/fuji/sky_block`.

    And issue: `/world import sky_block minecraft:overworld --seed \\<seed\\> --chunkGeneratorType FLAT --chunkGeneratorParameters "minecraft:air;minecraft:the_void"`
    This defines a `runtime dimension descriptor` with `flat chunk generator`, and with `void layers`.
    The `void layers` means the `flat chunk generator` will fill `minecraft:air` when it is needed to `generate a new chunk`.

    ◉ Import a `overworld dimension`.
    Issue: `/world import my_overworld minecraft:overworld --seed \\<seed\\>`
    You need to specify the `dimension type` to define the `environment of your dimension`.
    And also the `seed` to define the `seed of your dimension`.

    ◉ Import a `superflat dimension`.
    Issue: `/world import my_superflat minecraft:overworld --seed \\<seed\\> --chunkGeneratorType FLAT`
    """)
@ColorBox(id = 1753243335351L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Use `command_bundle` module to create a `/tpw` command.
    The `/world tp` command is an `admin-level` command.
    You can use `command_bundle` module to create a `/tpw resource-world` command, to teleport players to `fuji:overworld`.

    ◉ Use `tppos` module to teleport the players to a specific world.
    1. `/tppos --dimension minecraft:overworld --x 0 --y 128 --z 0 --yaw 0 --pitch 0`
    2. `/tppos --dimension minecraft:overworld --centerX 0 --centerZ 0 --minRange 0 --maxRange 1000 --maxTryTimes 16`
    """)
@TestCase(action = "In MC 1.20.1, create a `overworld` dimension type with seed `12345`.", targets = {
    "Goto `/tp @s 14665 ~ 345`. (You should get `emerald * 7`, `gold ingot * 3`, `iron ingot * 11`, `tnt * 2`, `heart of the sea * 1`, `cooked cod * 8` and `potion of water breathing * 1`.)"
    , "Goto `/tp @s 0 128 0`, you should in `minecraft:ocean`, and there is a `minecraft:dark_forest` in front of you, also there is a `lava source` flowing down."
})
@TestCase(action = "Test the chunk generator types and parameters", targets = {
    "Issue `/world create flat minecraft:overworld --chunkGeneratorType FLAT`",
    "Issue `/world create void minecraft:overworld --chunkGeneratorType FLAT --chunkGeneratorParameters \"minecraft:air;minecraft:the_void\"`"
})


@CommandNode("world")
@CommandRequirement(level = 4)
public class WorldInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<WorldConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, WorldConfigModel.class);

    public static final BaseConfigurationHandler<WorldDataModel> world = ObjectConfigurationHandler
        .ofModule("world.json", WorldDataModel.class)
        .enableAutoSaveFeature(JobManager.CRON_EVERY_MINUTE);

    private static void ensureDimensionNotExists(@NotNull CommandSourceStack source, @NotNull IdentifierIR identifier) {
        if (WorldService.existsDimension(identifier)) {
            TextHelper.sendTextByKey(source, "world.dimension.exist");
            throw new AbortCommandExecutionException();
        }
    }

    private static void ensureDimensionIsNotVanillaDimension(@NotNull CommandSourceStack source, @NotNull String dimensionId) {
        if (WorldHelper.isVanillaDimension(dimensionId)) {
            TextHelper.sendTextByKey(source, "world.dimension.delete.forbidden", dimensionId);
            throw new AbortCommandExecutionException();
        }
    }

    @Document(id = 1751826609063L, value = "Teleport to the target dimension with the same coordinate.")
    @CommandNode("tp")
    private static int $tp(@CommandSource ServerPlayer player, Dimension dimension) {
        ServerLevel targetDimension = dimension.getValue();

        GlobalPos
            .of(player)
            .withLevel(RegistryHelper.getIdAsString(targetDimension))
            .teleport(player);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1752802990626L, value = """
        List `loaded dimensions` and `unloaded dimensions`.
        """)
    @CommandNode("list")
    private static int $list(@CommandSource CommandSourceStack source) {
        TextHelper.sendTextByKey(source, "dimension.loaded_dimensions");
        WorldHelper
            .getWorlds()
            .forEach(world -> {
                String dimensionId = RegistryHelper.getIdAsString(world);
                TextHelper.sendTextByKey(source, "dimension", dimensionId);
            });

        List<String> unloadedDimensions = WorldService
            .getRuntimeDimensionDescriptors()
            .stream()
            .filter(it -> !it.isDimensionLoaded())
            .map(RuntimeDimensionDescriptor::getDimension)
            .toList();
        TextHelper.sendTextByKey(source, "dimension.unloaded_dimensions", unloadedDimensions);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1752798248863L, value = """
        This command does the following things:
        1. `Add` a `dimension descriptor` into the `config` file.
        2. Use that `dimension descriptor` to `make` the `runtime dimension`.
        3. `Load` the runtime dimension into the `server`.
        """)
    @CommandNode("create")
    private static int $create(@CommandSource CommandSourceStack source
        , FujiIdentifier name
        , DimensionType dimensionType
        , Optional<Long> seed
        , Optional<ChunkGeneratorType> chunkGeneratorType
        , Optional<String> chunkGeneratorParameters
        , Optional<WorldPresetType> worldPresetType) {

        /* Make identifier for the new dimension. */
        IdentifierIR dimensionIdentifier = name.getValue();
        ensureDimensionNotExists(source, dimensionIdentifier);

        /* Make the runtime dimension descriptor. */
        long $seed = seed.orElse(RandomSupport.generateUniqueSeed());
        ChunkGeneratorType $chunkGeneratorType = chunkGeneratorType.orElse(ChunkGeneratorType.NOISE);
        IdentifierIR dimensionTypeIdentifier = IdentifierIR.makeIdentifierOrThrow(dimensionType.getValue());
        String $chunkGeneratorParameter = chunkGeneratorParameters.orElse("");
        WorldPresetType $worldPresetType = worldPresetType.orElse(null);
        RuntimeDimensionDescriptor runtimeDimensionDescriptor = RuntimeDimensionImporter.importRuntimeDimensionDescriptor(dimensionIdentifier, $worldPresetType, dimensionTypeIdentifier, $chunkGeneratorType, $chunkGeneratorParameter, $seed);

        /* Request to create the dimension. */
        DimensionCreationTicket ticket = new DimensionCreationTicket(source, runtimeDimensionDescriptor);
        WorldService.submitDimensionCreationTicket(ticket);
        return CommandHelper.Return.SUCCESS;
    }

    @SneakyThrows(IOException.class)
    @Document(id = 1752809824729L, value = """
        This command will `import` an external `dimension directory` placed in `world/dimensions/fuji/\\<dimension-name\\>`.
        You need to provide enough information to define the `runtime dimension descriptor`.

        <green>NOTE: This command is almost identical to `/world create` command.
        """)
    @CommandNode("import")
    private static int $import(@CommandSource CommandSourceStack source
        , FujiIdentifier name
        , DimensionType dimensionType
        , Optional<Long> seed
        , Optional<ChunkGeneratorType> chunkGeneratorType
        , Optional<String> chunkGeneratorParameters
        , Optional<WorldPresetType> worldPresetType) {

        /* Ensure the dimension dir existed. */
        IdentifierIR dimensionId = name.getValue();
        Path targetDimensionPath = RuntimeDimensionImporter.getLevelSavePath()
            .resolve("dimensions")
            .resolve(dimensionId.getNamespace())
            .resolve(dimensionId.getPath());

        if (!Files.exists(targetDimensionPath)) {
            TextHelper.sendTextByKey(source, "world.dimension.import.dimension_dir_not_found", targetDimensionPath.toFile().getCanonicalPath());
            return CommandHelper.Return.FAILURE;
        }

        /* Ensure seed existed. */
        if (seed.isEmpty()) {
            TextHelper.sendTextByKey(source, "dimension.seed.empty");
            return CommandHelper.Return.FAILURE;
        }

        return $create(source, name, dimensionType, seed, chunkGeneratorType, chunkGeneratorParameters, worldPresetType);
    }

    @Document(id = 1752798163284L, value = """
        This command does the following things:
        1. `Unload` the `loaded runtime dimension` in the server.
        2. <red><b>Delete the chunk files of the dimension.
        3. <red><b>Delete the `runtime dimension descriptor` in config file.
        """)
    @CommandNode("delete")
    private static int $delete(@CommandSource CommandSourceStack source, Dimension dimension, Optional<Boolean> confirm) {
        return CommandHelper.Pattern.withCommandConfirmed(source, confirm, () -> {
            ServerLevel $dimension = dimension.getValue();
            String dimensionId = RegistryHelper.getIdAsString($dimension);
            ensureDimensionIsNotVanillaDimension(source, dimensionId);

            /* Request to delete. */
            WorldService.submitDimensionDeletionTicket(new DimensionDeletionTicket(source, $dimension, true, true));
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1752798473110L, value = """
        This command does the following things:
        1. `Make` the `runtime dimension` instance based on the `runtime dimension descriptor`.
        2. `Load` the made `runtime dimension` into the `server`.
        """)
    @CommandNode("load")
    private static int $load(@CommandSource CommandSourceStack source, UnloadedRuntimeDimensionDescriptor dimension) {
        RuntimeDimensionDescriptor runtimeDimensionDescriptor = dimension.getValue();
        DimensionCreationTicket ticket = new DimensionCreationTicket(source, runtimeDimensionDescriptor);
        WorldService.submitDimensionCreationTicket(ticket);

        TextHelper.sendTextByKey(source, "world.dimension.load", runtimeDimensionDescriptor.dimension);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("unload")
    private static int $unload(@CommandSource CommandSourceStack source, LoadedRuntimeDimensionDescriptor dimension) {
        RuntimeDimensionDescriptor runtimeDimensionDescriptor = dimension.getValue();
        Optional<ServerLevel> loadedWorld = runtimeDimensionDescriptor.getLoadedWorld();
        String dimensionId = runtimeDimensionDescriptor.dimension;
        return loadedWorld
            .map($loadedWorld -> {
                WorldService.submitDimensionDeletionTicket(new DimensionDeletionTicket(source, $loadedWorld, false, false));
                TextHelper.sendTextByKey(source, "world.dimension.unload", dimensionId);
                return CommandHelper.Return.SUCCESS;
            })
            .orElseGet(() -> {
                TextHelper.sendTextByKey(source, "world.dimension.unload.already", dimensionId);
                return CommandHelper.Return.FAILURE;
            });
    }

    @Document(id = 1751826611302L, value = "Delete and create the specified world.")
    @CommandNode("reset")
    private static int $reset(@CommandSource CommandSourceStack source, Dimension dimension, Optional<Boolean> useTheSameSeed, Optional<Boolean> confirm) {
        return CommandHelper.Pattern.withCommandConfirmed(source, confirm, () -> {
            /* Get the original dimension node. */
            ServerLevel dimensionInstance = dimension.getValue();
            String dimensionIdentifier = RegistryHelper.getIdAsString(dimensionInstance);
            Optional<RuntimeDimensionDescriptor> runtimeDimensionDescriptor = WorldService.getRuntimeDimensionDescriptor(dimensionIdentifier);
            return runtimeDimensionDescriptor
                .map($runtimeDimensionDescriptor -> {
                    /* Delete the dimension instance. */
                    WorldService.submitDimensionDeletionTicket(new DimensionDeletionTicket(source, dimensionInstance, true, false));

                    /* Draw the seed. */
                    Boolean $useTheSameSeed = useTheSameSeed.orElse(false);
                    $runtimeDimensionDescriptor.seed = $useTheSameSeed ? $runtimeDimensionDescriptor.seed : RandomSupport.generateUniqueSeed();
                    world.writeStorage();

                    /* Create a new dimension instance. */
                    DimensionCreationTicket ticket = new DimensionCreationTicket(source, $runtimeDimensionDescriptor);
                    WorldService.submitDimensionCreationTicket(ticket);

                    TextHelper.sendBroadcastByKey("world.dimension.reset", dimensionIdentifier);
                    return CommandHelper.Return.SUCCESS;
                })
                .orElseGet(() -> {
                    TextHelper.sendTextByKey(source, "world.dimension.dimension_descriptor_not_found", dimensionIdentifier);
                    return CommandHelper.Return.FAILURE;
                });
        });
    }


    @Document(id = 1752248825291L, value = """
        Saves all the `dimension descriptors` from `memory` into the `storage`.
        """)
    @CommandNode("save-configs")
    @CommandRequirement(level = 4)
    private static int $saveConfigs(@CommandSource CommandSourceStack source) {
        WorldService.saveRuntimeDimensionDescriptors();
        TextHelper.sendTextByKey(source, "operation.success");
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1752433782557L, value = """
        List the dimensions each player is in.
        """)
    @CommandNode("who")
    @CommandRequirement(level = 4)
    private static int $who(@CommandSource CommandSourceStack source) {
        printGroupedPlayersByDimension(source, null);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1752434557605L, value = """
        List the players in specified dimension.
        """)
    @CommandNode("who")
    @CommandRequirement(level = 4)
    private static int $who(@CommandSource CommandSourceStack source, Dimension dimension) {
        printGroupedPlayersByDimension(source, dimension);
        return CommandHelper.Return.SUCCESS;
    }

    private static void printGroupedPlayersByDimension(CommandSourceStack source, @Nullable Dimension specifiedDimension) {
        Map<@NotNull String, List<String>> groupedPlayers = WorldHelper
            .getWorlds()
            .stream()
            .collect(Collectors.toMap(
                RegistryHelper::getIdAsString
                , world -> world.players().stream().map(PlayerHelper::getPlayerName).toList()));

        groupedPlayers
            .entrySet()
            .stream()
            .filter(entry -> {
                if (specifiedDimension == null) return true;
                String filterDimension = RegistryHelper.getIdAsString(specifiedDimension.getValue());
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
    private static int $info(@CommandSource CommandSourceStack source, Dimension dimension) {
        ServerLevel dimensionInstance = dimension.getValue();

        /* Make text for basic info. */
        TextHelper.sendTextByKey(source, "dimension.class", dimensionInstance.getClass().getName());
        TextHelper.sendTextByKey(source, "dimension.id", RegistryHelper.getIdAsString(dimensionInstance));
        TextHelper.sendTextByKey(source, "dimension.difficulty", dimensionInstance.getDifficulty());
        TextHelper.sendTextByKey(source, "dimension.seed", dimensionInstance.getSeed());
        TextHelper.sendTextByKey(source, "dimension.options", dimensionInstance.dimensionType());
        TextHelper.sendTextByKey(source, "dimension.properties", dimensionInstance.getLevelData());
        TextHelper.sendTextByKey(source, "dimension.chunk_generator", dimensionInstance.getChunkSource().getGenerator());

        /* Make text for dimension border. */
        WorldBorder worldBorder = dimensionInstance.getWorldBorder();
        TextHelper.sendTextByKey(source, "dimension.border");
        TextHelper.sendTextByKey(source, "dimension.border.size", worldBorder.getSize());
        TextHelper.sendTextByKey(source, "dimension.border.size.lerp_target", worldBorder.getLerpTarget());
        TextHelper.sendTextByKey(source, "dimension.border.size.lerp_time", WorldBorderHelper.getLerpTime(worldBorder));
        TextHelper.sendTextByKey(source, "dimension.border.center.x", worldBorder.getCenterX());
        TextHelper.sendTextByKey(source, "dimension.border.center.z", worldBorder.getCenterZ());
        TextHelper.sendTextByKey(source, "dimension.border.damage.per_block", worldBorder.getDamagePerBlock());
        TextHelper.sendTextByKey(source, "dimension.border.safe_zone", WorldBorderHelper.getSafeZone(worldBorder));
        TextHelper.sendTextByKey(source, "dimension.border.warning.blocks", worldBorder.getWarningBlocks());
        TextHelper.sendTextByKey(source, "dimension.border.warning.time", worldBorder.getWarningTime());

        /* Make text for gamerules. */
        MutableComponent gameRulesText = TextHelper.getTextByKey(source, "dimension.gamerules").copy();
        Map<String, Object> gameRulesMap = new HashMap<>();
        GameRules gameRules = dimensionInstance.getGameRules();
        gameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            @Override
            public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                String gameRuleName = key.getId();
                T gameRuleValue = gameRules.getRule(key);
                gameRulesMap.put(gameRuleName, gameRuleValue);
            }
        });
        MutableComponent gameRulesHoverText = TextHelper.Formatter.formatMapInLine(gameRulesMap);
        gameRulesText
            .withStyle(Style.EMPTY
                .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(gameRulesHoverText)));
        TextHelper.sendMessageByText(source, gameRulesText);
        TextHelper.sendTextByKey(source, "prompt.hover.see_it");

        return CommandHelper.Return.SUCCESS;
    }

    @Override
    protected void registerGsonTypeAdapters() {
        GsonMapper.setTypeNullability(Difficulty.class, false);
    }
}

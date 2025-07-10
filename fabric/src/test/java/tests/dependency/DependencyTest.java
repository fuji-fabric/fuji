package tests.dependency;

import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.core.CoreInitializer;
import io.github.sakurawald.fuji.module.mixin.GlobalMixinConfigPlugin;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.SlotRange;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.message.MessageType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.predicate.NumberRange;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.UserCache;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import tests.dependency.structure.DependencyNode;
import tests.dependency.structure.FileDependencyChecker;
import tests.dependency.structure.ModuleDependencyChecker;

@ForDeveloper("""
    You may ask why we are so strict with the symbol reference, it's mainly because the loading mechanism of JVM.

    When you reference a symbol, it will trigger the loading of mixins, which introduces the possibility to crash the server.
    Especially when the server is not initialized fully.
    """)
public class DependencyTest {

    private static final String PROJECT_ROOT_PACKAGE_NAME = Fuji.class.getPackageName();
    private static final String PROJECT_CORE_PACKAGE_NAME = PROJECT_ROOT_PACKAGE_NAME + ".core";
    private static final String PROJECT_MODULE_PACKAGE_NAME = PROJECT_ROOT_PACKAGE_NAME + ".module";

    private static final Path COMPILE_TIME_JAVA_SOURCE_PATH = Path.of("src", "main", "java");
    private static final Path COMPILE_TIME_MAIN_FUNCTION_PACKAGE_PATH = COMPILE_TIME_JAVA_SOURCE_PATH.resolve(PROJECT_ROOT_PACKAGE_NAME.replace(".", "/"));
    private static final Path COMPILE_TIME_CORE_PACKAGE_PATH = COMPILE_TIME_MAIN_FUNCTION_PACKAGE_PATH.resolve("core");

//    private static final Path COMPILE_TIME_CORE_CONFIG_PACKAGE_PATH = COMPILE_TIME_CORE_PACKAGE_PATH.resolve("config");

    private static @NotNull String[] getAllowedPackagesInCore() {
        List<String> allowedPackages = new ArrayList<>();

        /* Handle special class files. */
        allowedPackages.add(Fuji.class.getPackage().getName());
        allowedPackages.add(ModuleInitializer.class.getPackage().getName());
        allowedPackages.add(CoreInitializer.class.getPackage().getName());
        allowedPackages.add(GlobalMixinConfigPlugin.class.getName());

        /* Treat the classes inside core package as a whole. */
        allowedPackages.add(PROJECT_CORE_PACKAGE_NAME);

        /* Allowed non-Minecraft libraries. */
        allowedPackages.add("java.");
        allowedPackages.add("org.jetbrains.");
        allowedPackages.add("lombok.");
        allowedPackages.add("com.google.gson.");
        allowedPackages.add("com.google.common.");
        allowedPackages.add("org.quartz.");
        allowedPackages.add("com.jayway.jsonpath.");
        allowedPackages.add("org.apache.");

        /* Allowed Minecraft libraries. */
        allowedPackages.add("eu.pb4.");
        allowedPackages.add("net.luckperms.api.");
        allowedPackages.add("com.mojang.brigadier.");
        allowedPackages.add("com.mojang.authlib.");
        allowedPackages.add("net.fabricmc.api.");
        allowedPackages.add("net.fabricmc.loader.api.");
        allowedPackages.add("org.spongepowered.asm.");

        /* Allowed Minecraft entities. */
        allowedPackages.add(MinecraftServer.class.getName());
        allowedPackages.add(PlayerManager.class.getName());
        allowedPackages.add(UserCache.class.getName());

        allowedPackages.add(Registry.class.getName());
        allowedPackages.add(Registries.class.getName());
        allowedPackages.add(RegistryKeys.class.getName());
        allowedPackages.add(RegistryKey.class.getName());
        allowedPackages.add(RegistryEntry.class.getName());
        allowedPackages.add(Identifier.class.getName());
        allowedPackages.add(SimpleRegistry.class.getName());

        allowedPackages.add("net.minecraft.nbt.");
        allowedPackages.add("net.minecraft.storage.ReadView");
        allowedPackages.add(ErrorReporter.class.getName());

        allowedPackages.add("net.minecraft.component.");

        allowedPackages.add(MessageType.class.getName());


        allowedPackages.add(ServerPlayerEntity.class.getName());
        allowedPackages.add(PlayerEntity.class.getName());
        allowedPackages.add("net.minecraft.entity.Leashable");
        allowedPackages.add("net.minecraft.entity.decoration.BlockAttachedEntity");
        allowedPackages.add("net.minecraft.entity.decoration.LeashKnotEntity");
        allowedPackages.add("net.minecraft.entity.vehicle.AbstractMinecartEntity");
        allowedPackages.add("net.minecraft.entity.vehicle.BoatEntity");
        allowedPackages.add("net.minecraft.entity.vehicle.VehicleEntity");

        allowedPackages.add(ServerCommandSource.class.getName());
        allowedPackages.add(CommandManager.class.getName());
        allowedPackages.add(CommandRegistryAccess.class.getName());

        allowedPackages.add(Text.class.getName());
        allowedPackages.add(Style.class.getName());
        allowedPackages.add(Formatting.class.getName());
        allowedPackages.add(MutableText.class.getName());
        allowedPackages.add(ClickEvent.class.getName());

        allowedPackages.add(ItemStack.class.getName());

        allowedPackages.add(World.class.getName());
        allowedPackages.add(ServerWorld.class.getName());
        allowedPackages.add(Chunk.class.getName());
        allowedPackages.add(ChunkPos.class.getName());
        allowedPackages.add(ChunkHolder.class.getName());
        allowedPackages.add(ChunkSectionPos.class.getName());
        allowedPackages.add(Direction.class.getName());
        allowedPackages.add(DimensionType.class.getName());
        allowedPackages.add(DimensionTypes.class.getName());

        allowedPackages.add(Vec3d.class.getName());
        allowedPackages.add(Vec2f.class.getName());

        allowedPackages.add(Item.class.getName());

        allowedPackages.add(Block.class.getName());
        allowedPackages.add(Blocks.class.getName());
        allowedPackages.add(BlockPos.class.getName());
        allowedPackages.add(BlockItem.class.getName());
        allowedPackages.add(BlockState.class.getName());

        allowedPackages.add(Entity.class.getName());
        allowedPackages.add(MobEntity.class.getName());

        allowedPackages.add(DamageSource.class.getName());

        allowedPackages.add(BossBar.class.getName());
        allowedPackages.add(ServerBossBar.class.getName());

        allowedPackages.add(ScreenHandlerType.class.getName());
        allowedPackages.add(GenericContainerScreenHandler.class.getName());
        allowedPackages.add(EquipmentSlot.class.getName());
        allowedPackages.add(SimpleInventory.class.getName());
        allowedPackages.add(DefaultedList.class.getName());

        allowedPackages.add(DyeColor.class.getName());

        allowedPackages.add(SoundEvent.class.getName());
        allowedPackages.add(SoundCategory.class.getName());

        allowedPackages.add("net.minecraft.network.packet.");
        allowedPackages.add(ServerPlayNetworkHandler.class.getName());

        allowedPackages.add("net.minecraft.command.argument.");
        allowedPackages.add("net.minecraft.scoreboard.ScoreboardDisplaySlot");
        allowedPackages.add("net.minecraft.scoreboard.ScoreboardObjective");
        allowedPackages.add(NumberRange.class.getName());
        allowedPackages.add(SlotRange.class.getName());
        allowedPackages.add(ColumnPos.class.getName());
        allowedPackages.add(Team.class.getName());
        allowedPackages.add(ParticleEffect.class.getName());
        allowedPackages.add(GameMode.class.getName());
        allowedPackages.add(CommandFunction.class.getName());
        allowedPackages.add(ScoreboardCriterion.class.getName());

        allowedPackages.add(ShulkerBoxBlock.class.getName());

        return allowedPackages.toArray(new String[0]);
    }

    @Test
//    @Disabled("Enable this test to see the detailed result of dependency nodes.")
    public void listFileDependencies() {
        new FileDependencyChecker()
            .makeDependencyNodes(COMPILE_TIME_JAVA_SOURCE_PATH)
            .forEach(System.out::println);
    }

    @Test
    public void banDirectReferencesBetweenModules() {
        List<DependencyNode> dependencyNodes = new ModuleDependencyChecker()
            .makeDependencyNodes(COMPILE_TIME_JAVA_SOURCE_PATH);
        DependencyNode.tryReportViolationDependencyNodes(dependencyNodes, "One module references other modules directly.");
    }

    @Test
    public void banDirectReferencesBetweenCoreAndModules() {
        List<DependencyNode> violationNodes = new FileDependencyChecker()
            .makeDependencyNodes(COMPILE_TIME_CORE_PACKAGE_PATH)
            .stream()
            .filter(node -> {
                /* Only care classes from this project. */
                node.includeReference(
                    PROJECT_MODULE_PACKAGE_NAME
                );

                /* Allow the core to reference these classes directly. */
                node.excludeReference(getAllowedPackagesInCore());

                return !node.reference.isEmpty();
            })
            .toList();

        DependencyNode.tryReportViolationDependencyNodes(violationNodes, "The `core` package should not reference the `module` package.");
    }

    @Test
    public void banUnnecessaryImportsInMainControlModel() {

    }


    @Test
    public void banUnnecessaryImportsInCoreConfigPackage() {
        List<DependencyNode> violationNodes = new FileDependencyChecker()
            .makeDependencyNodes(COMPILE_TIME_CORE_PACKAGE_PATH)
            .stream()
            .filter(node -> {
                /* Only allow to reference these symbols in main control file, to avoid early class loading. */
                node.excludeReference(getAllowedPackagesInCore());
                return !node.reference.isEmpty();
            })
            .toList();

        DependencyNode.tryReportViolationDependencyNodes(violationNodes, "The `core.config` package references banned packages.");
    }

}

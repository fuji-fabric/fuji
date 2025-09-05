package tests.dependency;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.config.model.MainControlConfigModel;
import io.github.sakurawald.fuji.core.document.annotation.Document;
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
import org.junit.jupiter.api.Disabled;
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

    private static final Path COMPILE_TIME_MAIN_CONTROL_FILE_PATH = COMPILE_TIME_JAVA_SOURCE_PATH.resolve(MainControlConfigModel.class.getName().replace(".", "/") + ".java");

    private static @NotNull String[] getAllowedReferencesInCore() {
        List<String> allowedReferences = new ArrayList<>();

        /* Handle special class files. */
        allowedReferences.add(Fuji.class.getPackage().getName());
        allowedReferences.add(ModuleInitializer.class.getPackage().getName());
        allowedReferences.add(CoreInitializer.class.getPackage().getName());
        allowedReferences.add(GlobalMixinConfigPlugin.class.getName());

        /* Treat the classes inside core package as a whole. */
        allowedReferences.add(PROJECT_CORE_PACKAGE_NAME);

        /* Allowed non-Minecraft libraries. */
        allowedReferences.add("java.");
        allowedReferences.add("org.jetbrains.");
        allowedReferences.add("lombok.");
        allowedReferences.add("com.google.gson.");
        allowedReferences.add("com.google.common.");
        allowedReferences.add("org.quartz.");
        allowedReferences.add("com.jayway.jsonpath.");
        allowedReferences.add("org.apache.");

        /* Allowed Minecraft libraries. */
        allowedReferences.add("eu.pb4.");
        allowedReferences.add("net.luckperms.api.");
        allowedReferences.add("com.mojang.brigadier.");
        allowedReferences.add("com.mojang.authlib.");
        allowedReferences.add("net.fabricmc.api.");
        allowedReferences.add("net.fabricmc.loader.api.");
        allowedReferences.add("org.spongepowered.asm.");

        /* Allowed Minecraft entities. */
        allowedReferences.add(MinecraftServer.class.getName());
        allowedReferences.add(PlayerManager.class.getName());
        allowedReferences.add(UserCache.class.getName());

        allowedReferences.add(Registry.class.getName());
        allowedReferences.add(Registries.class.getName());
        allowedReferences.add(RegistryKeys.class.getName());
        allowedReferences.add(RegistryKey.class.getName());
        allowedReferences.add(RegistryEntry.class.getName());
        allowedReferences.add(Identifier.class.getName());
        allowedReferences.add(SimpleRegistry.class.getName());

        allowedReferences.add("net.minecraft.nbt.");
        allowedReferences.add("net.minecraft.storage.ReadView");
        allowedReferences.add("net.minecraft.util.ErrorReporter");
        allowedReferences.add("net.minecraft.component.");

        allowedReferences.add(MessageType.class.getName());


        allowedReferences.add(ServerPlayerEntity.class.getName());
        allowedReferences.add(PlayerEntity.class.getName());
        allowedReferences.add("net.minecraft.entity.Leashable");
        allowedReferences.add("net.minecraft.entity.decoration.BlockAttachedEntity");
        allowedReferences.add("net.minecraft.entity.decoration.LeashKnotEntity");
        allowedReferences.add("net.minecraft.entity.vehicle.AbstractMinecartEntity");
        allowedReferences.add("net.minecraft.entity.vehicle.BoatEntity");
        allowedReferences.add("net.minecraft.entity.vehicle.VehicleEntity");

        allowedReferences.add(ServerCommandSource.class.getName());
        allowedReferences.add(CommandManager.class.getName());
        allowedReferences.add(CommandRegistryAccess.class.getName());

        allowedReferences.add(Text.class.getName());
        allowedReferences.add(Style.class.getName());
        allowedReferences.add(Formatting.class.getName());
        allowedReferences.add(MutableText.class.getName());
        allowedReferences.add(ClickEvent.class.getName());

        allowedReferences.add(ItemStack.class.getName());

        allowedReferences.add(World.class.getName());
        allowedReferences.add(ServerWorld.class.getName());
        allowedReferences.add(Chunk.class.getName());
        allowedReferences.add(ChunkPos.class.getName());
        allowedReferences.add(ChunkHolder.class.getName());
        allowedReferences.add(ChunkSectionPos.class.getName());
        allowedReferences.add(Direction.class.getName());
        allowedReferences.add(DimensionType.class.getName());
        allowedReferences.add(DimensionTypes.class.getName());

        allowedReferences.add(Vec3d.class.getName());
        allowedReferences.add(Vec2f.class.getName());

        allowedReferences.add(Item.class.getName());

        allowedReferences.add(Block.class.getName());
        allowedReferences.add(Blocks.class.getName());
        allowedReferences.add(BlockPos.class.getName());
        allowedReferences.add(BlockItem.class.getName());
        allowedReferences.add(BlockState.class.getName());

        allowedReferences.add(Entity.class.getName());
        allowedReferences.add(MobEntity.class.getName());

        allowedReferences.add(DamageSource.class.getName());

        allowedReferences.add(BossBar.class.getName());
        allowedReferences.add(ServerBossBar.class.getName());

        allowedReferences.add(ScreenHandlerType.class.getName());
        allowedReferences.add(GenericContainerScreenHandler.class.getName());
        allowedReferences.add(EquipmentSlot.class.getName());
        allowedReferences.add(SimpleInventory.class.getName());
        allowedReferences.add(DefaultedList.class.getName());

        allowedReferences.add(DyeColor.class.getName());

        allowedReferences.add(SoundEvent.class.getName());
        allowedReferences.add(SoundCategory.class.getName());

        allowedReferences.add("net.minecraft.network.packet.");
        allowedReferences.add(ServerPlayNetworkHandler.class.getName());

        allowedReferences.add("net.minecraft.command.argument.");
        allowedReferences.add("net.minecraft.scoreboard.ScoreboardDisplaySlot");
        allowedReferences.add("net.minecraft.scoreboard.ScoreboardObjective");
        allowedReferences.add(NumberRange.class.getName());
        allowedReferences.add("net.minecraft.inventory.SlotRange");
        allowedReferences.add(ColumnPos.class.getName());
        allowedReferences.add(Team.class.getName());
        allowedReferences.add(ParticleEffect.class.getName());
        allowedReferences.add(GameMode.class.getName());
        allowedReferences.add(CommandFunction.class.getName());
        allowedReferences.add(ScoreboardCriterion.class.getName());

        allowedReferences.add(ShulkerBoxBlock.class.getName());

        return allowedReferences.toArray(new String[0]);
    }

    @Test
    @Disabled("Enable this test to see the detailed result of dependency nodes.")
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
                node.excludeReference(getAllowedReferencesInCore());

                return !node.reference.isEmpty();
            })
            .toList();

        DependencyNode.tryReportViolationDependencyNodes(violationNodes, "The `core` package should not reference the `module` package.");
    }

    @Test
    public void banUnnecessaryImportsInMainControlModel() {
        List<DependencyNode> violationNodes = new FileDependencyChecker()
            .makeDependencyNodes(COMPILE_TIME_MAIN_CONTROL_FILE_PATH)
            .stream()
            .filter(node -> {
                /* Only allow to reference these symbols in main control file, to avoid early class loading. */
                node.excludeReference(
                    SerializedName.class.getName()
                    , Document.class.getName()
                    , List.class.getName()
                    , ArrayList.class.getName());
                return !node.reference.isEmpty();
            })
            .toList();

        DependencyNode.tryReportViolationDependencyNodes(violationNodes, "The `Main Config File` package references banned packages.");
    }

}

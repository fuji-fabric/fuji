package tests.dependency;

import com.google.gson.annotations.SerializedName;
import mod.fuji.Fuji;
import mod.fuji.core.config.model.MainControlConfigModel;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.core.CoreInitializer;
import mod.fuji.module.mixin.GlobalMixinConfigPlugin;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import tests.dependency.structure.DependencyNode;
import tests.dependency.structure.FileDependencyChecker;
import tests.dependency.structure.ModuleDependencyChecker;

/**
 *     You may ask why we are so strict with the symbol reference, it's mainly because the loading mechanism of JVM.

    When you reference a symbol, it will trigger the loading of mixins, which introduces the possibility to crash the server.
    Especially when the server is not initialized fully.

 **/
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
        allowedReferences.add(PlayerList.class.getName());

        allowedReferences.add(Registry.class.getName());
        allowedReferences.add(BuiltInRegistries.class.getName());
        allowedReferences.add(Registries.class.getName());
        allowedReferences.add(ResourceKey.class.getName());
        allowedReferences.add(Holder.class.getName());
        allowedReferences.add(ResourceLocation.class.getName());
        allowedReferences.add(MappedRegistry.class.getName());

        allowedReferences.add("net.minecraft.nbt.");
        allowedReferences.add("net.minecraft.storage.ReadView");
        allowedReferences.add("net.minecraft.util.ErrorReporter");
        allowedReferences.add("net.minecraft.component.");

        allowedReferences.add(ChatType.class.getName());


        allowedReferences.add(ServerPlayer.class.getName());
        allowedReferences.add(Player.class.getName());
        allowedReferences.add("net.minecraft.entity.Leashable");
        allowedReferences.add("net.minecraft.entity.decoration.BlockAttachedEntity");
        allowedReferences.add("net.minecraft.entity.decoration.LeashKnotEntity");
        allowedReferences.add("net.minecraft.entity.vehicle.AbstractMinecartEntity");
        allowedReferences.add("net.minecraft.entity.vehicle.BoatEntity");
        allowedReferences.add("net.minecraft.entity.vehicle.VehicleEntity");

        allowedReferences.add(CommandSourceStack.class.getName());
        allowedReferences.add(Commands.class.getName());
        allowedReferences.add(CommandBuildContext.class.getName());

        allowedReferences.add(Component.class.getName());
        allowedReferences.add(Style.class.getName());
        allowedReferences.add(ChatFormatting.class.getName());
        allowedReferences.add(MutableComponent.class.getName());
        allowedReferences.add(ClickEvent.class.getName());

        allowedReferences.add(ItemStack.class.getName());

        allowedReferences.add(Level.class.getName());
        allowedReferences.add(ServerLevel.class.getName());
        allowedReferences.add(ChunkAccess.class.getName());
        allowedReferences.add(ChunkPos.class.getName());
        allowedReferences.add(ChunkHolder.class.getName());
        allowedReferences.add(SectionPos.class.getName());
        allowedReferences.add(Direction.class.getName());
        allowedReferences.add(DimensionType.class.getName());
        allowedReferences.add(BuiltinDimensionTypes.class.getName());

        allowedReferences.add(Vec3.class.getName());
        allowedReferences.add(Vec2.class.getName());

        allowedReferences.add(Item.class.getName());

        allowedReferences.add(Block.class.getName());
        allowedReferences.add(Blocks.class.getName());
        allowedReferences.add(BlockPos.class.getName());
        allowedReferences.add(BlockItem.class.getName());
        allowedReferences.add(BlockState.class.getName());

        allowedReferences.add(Entity.class.getName());
        allowedReferences.add(Mob.class.getName());

        allowedReferences.add(DamageSource.class.getName());

        allowedReferences.add(BossEvent.class.getName());
        allowedReferences.add(ServerBossEvent.class.getName());

        allowedReferences.add(MenuType.class.getName());
        allowedReferences.add(ChestMenu.class.getName());
        allowedReferences.add(EquipmentSlot.class.getName());
        allowedReferences.add(SimpleContainer.class.getName());
        allowedReferences.add(NonNullList.class.getName());

        allowedReferences.add(DyeColor.class.getName());

        allowedReferences.add(SoundEvent.class.getName());
        allowedReferences.add(SoundSource.class.getName());

        allowedReferences.add("net.minecraft.network.packet.");
        allowedReferences.add(ServerGamePacketListenerImpl.class.getName());

        allowedReferences.add("net.minecraft.command.argument.");
        allowedReferences.add("net.minecraft.scoreboard.ScoreboardDisplaySlot");
        allowedReferences.add("net.minecraft.scoreboard.ScoreboardObjective");
        allowedReferences.add(MinMaxBounds.class.getName());
        allowedReferences.add("net.minecraft.inventory.SlotRange");
        allowedReferences.add(ColumnPos.class.getName());
        allowedReferences.add(PlayerTeam.class.getName());
        allowedReferences.add(ParticleOptions.class.getName());
        allowedReferences.add(GameType.class.getName());
        allowedReferences.add(CommandFunction.class.getName());
        allowedReferences.add(ObjectiveCriteria.class.getName());

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

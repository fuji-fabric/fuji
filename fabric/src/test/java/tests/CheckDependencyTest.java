package tests;

import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.mixin.GlobalMixinConfigPlugin;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.junit.jupiter.api.Test;
import structure.dependency_checker.Dependency;
import structure.dependency_checker.FileDependencyChecker;
import structure.dependency_checker.ModuleDependencyChecker;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * You may ask why we are so strict with the symbol reference, it's mainly because the loading mechanism of jvm.
 * Each time you `import` a file, the jvm will trigger the static initialization process, which also introduce the possibility to crash the server.
 * The import of symbols, trigger the loading of mixins, possibly registered by other mods.
 */
public class CheckDependencyTest {
    private static final Path COMPILE_TIME_SOURCE_PATH = Path.of("src", "main", "java");

    private static final String PROJECT_PACKAGE = "io.github.sakurawald";
    private static final String PROJECT_MODULE_PACKAGE = PROJECT_PACKAGE + ".module";

    private static final Path COMPILE_TIME_MAIN_PACKAGE_PATH = COMPILE_TIME_SOURCE_PATH.resolve(PROJECT_PACKAGE.replace(".", "/"));

    private static final String JAVA_PACKAGE = "java.";
    private static final String JETBRAINS_ANNOTATION_PACKAGE = "org.jetbrains";
    private static final String LOMBOK_PACKAGE = "lombok.";

    private static final String NET_MINECRAFT_PACKAGE = "net.minecraft";
    private static final String COM_MOJANG_PACKAGE = "com.mojang";

    private static final String[] MOJANG_PACKAGES = new String[]{COM_MOJANG_PACKAGE, NET_MINECRAFT_PACKAGE};
    private static final String[] BASE_PACKAGES = new String[]{
        JAVA_PACKAGE
        , JETBRAINS_ANNOTATION_PACKAGE
        , LOMBOK_PACKAGE
        , MinecraftServer.class.getName()
        , ServerPlayerEntity.class.getName()
    };

    @Test
    void testModuleDependency() {
        List<Dependency> dependencies = new ModuleDependencyChecker().makeDependencies(COMPILE_TIME_SOURCE_PATH);

        if (!dependencies.isEmpty()) {
            dependencies.forEach(System.out::println);
            throw new RuntimeException("one module references other modules.");
        }
    }

    @Test
    void testCoreDependency() {
        Stream<Dependency> dependencies = new FileDependencyChecker().makeDependencies(
                COMPILE_TIME_MAIN_PACKAGE_PATH.resolve("core"))
            .stream()
            .filter(dep -> {
                dep.filterReference(
                    PROJECT_MODULE_PACKAGE
                );
                dep.excludeReference(
                    ModuleInitializer.class.getName()
                    , GlobalMixinConfigPlugin.class.getName()
                );
                return !dep.getReference().isEmpty();
            });

        dependencies.forEach(dep -> {
            System.out.println(dep);
            throw new RuntimeException("the `core` package references the `module` package.");
        });
    }

    @Test
    void testCoreConfigDependency() {
        Stream<Dependency> dependencies = new FileDependencyChecker().makeDependencies(
                COMPILE_TIME_MAIN_PACKAGE_PATH.resolve("core").resolve("config"))
            .stream()
            .filter(dep -> {
                dep.filterReference(MOJANG_PACKAGES);
                return !dep.getReference().isEmpty();
            });

        dependencies.forEach(dep -> {
            System.out.println(dep);
            throw new RuntimeException("the `core.config` package references mojang classes.");
        });
    }

}

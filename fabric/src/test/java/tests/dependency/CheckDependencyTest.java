package tests.dependency;

import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.core.CoreInitializer;
import io.github.sakurawald.fuji.module.mixin.GlobalMixinConfigPlugin;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import tests.dependency.structure.DependencyNode;
import tests.dependency.structure.FileDependencyChecker;
import tests.dependency.structure.ModuleDependencyChecker;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@ForDeveloper("""
    You may ask why we are so strict with the symbol reference, it's mainly because the loading mechanism of JVM.

    When you reference a symbol, it will trigger the loading of mixins, which introduces the possibility to crash the server.
    Especially when the server is not initialized fully.
    """)
public class CheckDependencyTest {

    private static final String PROJECT_PACKAGE = Fuji.class.getPackageName();
    private static final String PROJECT_MODULE_PACKAGE = PROJECT_PACKAGE + ".module";

    private static final Path COMPILE_TIME_JAVA_SOURCE_PATH = Path.of("src", "main", "java");
    private static final Path COMPILE_TIME_MAIN_FUNCTION_PACKAGE_PATH = COMPILE_TIME_JAVA_SOURCE_PATH.resolve(PROJECT_PACKAGE.replace(".", "/"));
    public static final Path COMPILE_TIME_CORE_PACKAGE_PATH = COMPILE_TIME_MAIN_FUNCTION_PACKAGE_PATH.resolve("core");

    private static class WellKnownPackages {
        private static final String JAVA_PACKAGE = "java.";
        private static final String JETBRAINS_ANNOTATION_PACKAGE = "org.jetbrains";
        private static final String LOMBOK_PACKAGE = "lombok.";
        private static final String NET_MINECRAFT_PACKAGE = "net.minecraft";
        private static final String COM_MOJANG_PACKAGE = "com.mojang";
        private static final String[] MOJANG_PACKAGES = new String[]{COM_MOJANG_PACKAGE, NET_MINECRAFT_PACKAGE};
    }

    private static final String[] BASE_PACKAGES = new String[]{
        WellKnownPackages.JAVA_PACKAGE
        , WellKnownPackages.JETBRAINS_ANNOTATION_PACKAGE
        , WellKnownPackages.LOMBOK_PACKAGE
        , MinecraftServer.class.getName()
        , ServerPlayerEntity.class.getName()
    };

    @Test
    public void listFileDependencies() {
        new FileDependencyChecker()
            .makeDependencyNodes(COMPILE_TIME_JAVA_SOURCE_PATH)
            .forEach(System.out::println);
    }

    @Test
    void testModuleDependency() {
        List<DependencyNode> dependencies = new ModuleDependencyChecker().makeDependencyNodes(COMPILE_TIME_JAVA_SOURCE_PATH);

        if (!dependencies.isEmpty()) {
            dependencies.forEach(System.out::println);
            throw new RuntimeException("one module references other modules.");
        }
    }

    @Test
    void testCoreDependency() {
        Stream<DependencyNode> dependencies = new FileDependencyChecker().makeDependencyNodes(
                COMPILE_TIME_CORE_PACKAGE_PATH)
            .stream()
            .filter(dep -> {
                dep.includeReference(
                    PROJECT_MODULE_PACKAGE
                );
                dep.excludeReference(
                    ModuleInitializer.class.getName()
                    , GlobalMixinConfigPlugin.class.getName()
                    , CoreInitializer.class.getName()
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
        Stream<DependencyNode> dependencies = new FileDependencyChecker().makeDependencyNodes(
                COMPILE_TIME_CORE_PACKAGE_PATH.resolve("config"))
            .stream()
            .filter(dep -> {
                dep.includeReference(WellKnownPackages.MOJANG_PACKAGES);
                return !dep.getReference().isEmpty();
            });

        dependencies.forEach(dep -> {
            System.out.println(dep);
            throw new RuntimeException("the `core.config` package references mojang classes.");
        });
    }

}

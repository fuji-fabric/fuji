package tests.dependency;

import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.core.CoreInitializer;
import io.github.sakurawald.fuji.module.mixin.GlobalMixinConfigPlugin;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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
    private static final Path COMPILE_TIME_CORE_CONFIG_PACKAGE_PATH = COMPILE_TIME_CORE_PACKAGE_PATH.resolve("config");

    private static class WellKnownPackages {
        private static final String JAVA_PACKAGE_PREFIX = "java.";
        private static final String JETBRAINS_ANNOTATION_PACKAGE_PREFIX = "org.jetbrains.";
        private static final String LOMBOK_PACKAGE_PREFIX = "lombok.";
        private static final String GSON_PACKAGE_PREFIX = "com.google.gson.";
        private static final String QUARTZ_PACKAGE_PREFIX = "org.quartz.";
        private static final String JSON_PATH_PACKAGE_PREFIX = "com.jayway.jsonpath.";

        private static final String NET_MINECRAFT_PACKAGE_PREFIX = "net.minecraft.";
        private static final String COM_MOJANG_PACKAGE_PREFIX = "com.mojang.";
        private static final String[] MOJANG_PACKAGES_PREFIX = new String[]{COM_MOJANG_PACKAGE_PREFIX, NET_MINECRAFT_PACKAGE_PREFIX};
    }

    private static final String[] ALLOWED_PACKAGES_IN_CORE = new String[]{
        PROJECT_CORE_PACKAGE_NAME
        , Fuji.class.getPackage().getName()
        , ModuleInitializer.class.getPackage().getName()
        , CoreInitializer.class.getPackage().getName()
        , GlobalMixinConfigPlugin.class.getName()
        , WellKnownPackages.JAVA_PACKAGE_PREFIX
        , WellKnownPackages.JETBRAINS_ANNOTATION_PACKAGE_PREFIX
        , WellKnownPackages.LOMBOK_PACKAGE_PREFIX
        , WellKnownPackages.GSON_PACKAGE_PREFIX
        , WellKnownPackages.QUARTZ_PACKAGE_PREFIX
        , WellKnownPackages.JSON_PATH_PACKAGE_PREFIX
        , MinecraftServer.class.getName()
        , ServerPlayerEntity.class.getName()
    };

    @Test
//    @Disabled("Enable this test to see the detailed result of dependency nodes.")
    public void listFileDependencies() {
        new FileDependencyChecker()
            .makeDependencyNodes(COMPILE_TIME_JAVA_SOURCE_PATH)
            .forEach(System.out::println);
    }

    @Test
    void banDirectReferencesBetweenModules() {
        List<DependencyNode> dependencyNodes = new ModuleDependencyChecker()
            .makeDependencyNodes(COMPILE_TIME_JAVA_SOURCE_PATH);
        DependencyNode.tryReportViolationDependencyNodes(dependencyNodes, "One module references other modules directly.");
    }

    @Test
    void banDirectReferencesBetweenCoreAndModules() {
        List<DependencyNode> violationNodes = new FileDependencyChecker()
            .makeDependencyNodes(
                COMPILE_TIME_CORE_PACKAGE_PATH)
            .stream()
            .filter(node -> {
                /* Only care classes from this project. */
                node.includeReference(
                    PROJECT_MODULE_PACKAGE_NAME
                );

                /* Allow the core to reference these classes directly. */
                node.excludeReference(ALLOWED_PACKAGES_IN_CORE);

                return !node.reference.isEmpty();
            })
            .toList();

        DependencyNode.tryReportViolationDependencyNodes(violationNodes, "The `core` package should not reference the `module` package.");
    }

    @Test
    void banUnnecessaryImportsInCoreConfigPackage() {
        List<DependencyNode> violationNodes = new FileDependencyChecker()
            .makeDependencyNodes(COMPILE_TIME_CORE_CONFIG_PACKAGE_PATH)
            .stream()
            .filter(node -> {
                /* Only allow to reference these symbols in main control file, to avoid early class loading. */
                node.excludeReference(ALLOWED_PACKAGES_IN_CORE);
                return !node.reference.isEmpty();
            })
            .toList();

        DependencyNode.tryReportViolationDependencyNodes(violationNodes, "The `core.config` package references banned packages.");
    }

}

package tests.dependency.structure;

import mod.fuji.Fuji;
import mod.fuji.core.manager.impl.module.ModulePathResolver;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModuleDependencyChecker extends FileDependencyChecker {

    @SuppressWarnings("UnnecessaryLocalVariable")
    private @NotNull DependencyNode groupReferencesByModulePath(DependencyNode node) {
        /* Detect cross modules references. */
        String definitionClassName = node.definition;
        String definitionModulePath = ModulePathResolver.computeModulePathString(definitionClassName);
        List<String> bannedReferenceNames = new ArrayList<>();
        for (String referenceClassName : node.getReference()) {
            String referenceModulePath = ModulePathResolver.computeModulePathString(referenceClassName);

            /* Allow to reference symbols from core module. */
            if (referenceModulePath.equals(ModulePathResolver.CORE_MODULE_PATH_STRING)) continue;

            /* Allow to reference symbols from self-module or parent-module. */
            if (definitionModulePath.startsWith(referenceModulePath)) continue;

            bannedReferenceNames.add(referenceModulePath);
        }

        if (bannedReferenceNames.isEmpty()) {
            return DependencyNode.IGNORE_THIS_DEPENDENCY_NODE;
        }

        /* Return the violation node. */
        DependencyNode violationDependencyNode = new DependencyNode(definitionModulePath, bannedReferenceNames);
        return violationDependencyNode;
    }

    @Override
    public @NotNull DependencyNode makeDependencyNode(Path filePath) {
        /* Make the dependency nodes by file. */
        DependencyNode dependency = super.makeDependencyNode(filePath);

        /* We only cares the files from this project. */
        dependency.includeReference(Fuji.class.getPackageName());

        /* Group the references by module path. */
        return groupReferencesByModulePath(dependency);
    }
}

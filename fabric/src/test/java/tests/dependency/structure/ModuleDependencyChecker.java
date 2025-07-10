package tests.dependency.structure;

import com.google.gson.JsonElement;
import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.model.ConfigModel;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModuleDependencyChecker extends FileDependencyChecker {

    public static final JsonElement rcConfig = BaseConfigurationHandler.getGson().toJsonTree(new ConfigModel());

    private @Nullable Dependency groupSymbol(Dependency dep) {
        String definition = ModuleManager.computeJoinedModulePath(dep.getDefinition());
        List<String> referenceList = new ArrayList<>();

        for (String ref : dep.getReference()) {
            String reference = ModuleManager.computeJoinedModulePath(ref);
            // skip -> common reference
            if (reference.equals(ModuleManager.CORE_MODULE_NAME)) continue;
            // skip -> self reference
            if (definition.equals(reference)) continue;
            // skip -> parent reference
            if (definition.startsWith(reference)) continue;

            referenceList.add(reference);
        }

        if (definition.equals(ModuleManager.CORE_MODULE_NAME) || definition.equals("tester")) return null;
        if (referenceList.isEmpty()) return null;
        return new Dependency(definition, referenceList);
    }

    @Override
    public @Nullable Dependency makeDependency(Path file) {
        Dependency dependency = super.makeDependency(file);

        // filter: only collect project source file, and group them into modules.
        dependency.filterReference(Fuji.class.getPackageName());

        return groupSymbol(dependency);
    }
}

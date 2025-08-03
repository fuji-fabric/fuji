package io.github.sakurawald.fuji.core.config.transformer.impl;

import com.google.gson.JsonObject;
import com.jayway.jsonpath.DocumentContext;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.transformer.abst.JsonConfigurationTransformer;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;

import java.nio.file.Files;

@Deprecated
public class FlattenModulesTransformer extends JsonConfigurationTransformer {

    @Override
    public void apply() {
        if (Files.notExists(getTargetFilePath())) return;

        String modulesPath = "$.modules";
        DocumentContext context = getJsonDocumentContext();
        JsonObject modules = (JsonObject) getJsonPath(context, modulesPath);

        for (String topLevelModule : modules.keySet()) {
            FlattenTreeTransformer flattenTreeTransformer = new FlattenTreeTransformer(
                modulesPath + "." + topLevelModule
                , ModuleManager.ENABLE_SUPPLIER_KEY
                , topLevelModule
                , (walkingPath) -> ReflectionUtil.computeModuleConfigPath(walkingPath).resolve(BaseConfigurationHandler.CONFIG_JSON));

            flattenTreeTransformer.configure(this.getTargetFilePath());
            flattenTreeTransformer.apply();
        }
    }
}

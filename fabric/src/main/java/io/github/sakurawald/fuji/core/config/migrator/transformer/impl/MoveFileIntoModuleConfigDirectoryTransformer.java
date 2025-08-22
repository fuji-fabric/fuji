package io.github.sakurawald.fuji.core.config.migrator.transformer.impl;

import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;

import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;


@ForDeveloper("""
    This transformer is used to move a specified file into the module config directory of the specified module.
    """)
public class MoveFileIntoModuleConfigDirectoryTransformer extends MoveFileTransformer {

    public MoveFileIntoModuleConfigDirectoryTransformer(@NotNull Path sourceFile, @NotNull Class<?> moduleSpecifier) {
        super(sourceFile, ReflectionUtil.computeModuleConfigPath(moduleSpecifier));
    }
}

package io.github.sakurawald.fuji.core.config.transformer.impl;

import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;

import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import org.jetbrains.annotations.NotNull;


@ForDeveloper("""
    This transformer is used to move a specified file into the module config directory of the specified module.
    """)
public class MoveFileIntoModuleConfigDirectoryTransformer extends MoveFileTransformer {

    public MoveFileIntoModuleConfigDirectoryTransformer(@NotNull Class<?> moduleSpecifier) {
        super(ReflectionUtil.computeModuleConfigPath(moduleSpecifier));
    }
}

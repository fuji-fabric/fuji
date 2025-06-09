package io.github.sakurawald.core.config.transformer.impl;

import io.github.sakurawald.core.auxiliary.ReflectionUtil;

import java.nio.file.Path;


public class MoveFileIntoModuleConfigDirectoryTransformer extends MoveFileTransformer {

    public MoveFileIntoModuleConfigDirectoryTransformer(Path source, Class<?> clazz) {
        super(source, ReflectionUtil.computeModuleConfigPath(clazz));
    }
}

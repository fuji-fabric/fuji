package io.github.sakurawald.fuji.core.config.transformer.impl;

import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;

import java.nio.file.Path;


public class MoveFileIntoModuleConfigDirectoryTransformer extends MoveFileTransformer {

    public MoveFileIntoModuleConfigDirectoryTransformer(Path source, Class<?> clazz) {
        super(source, ReflectionUtil.computeModuleConfigPath(clazz));
    }
}

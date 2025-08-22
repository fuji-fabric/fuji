package io.github.sakurawald.fuji.core.config.handler.impl;

import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;


public class ObjectConfigurationHandler<T> extends BaseConfigurationHandler<T> {

    final @NotNull Class<T> typeOfModel;

    private ObjectConfigurationHandler(@NotNull Path filePath, @NotNull Class<T> typeOfModel) {
        super(filePath);
        this.typeOfModel = typeOfModel;
    }

    public static <T> @NotNull ObjectConfigurationHandler<T> ofPath(@NotNull Path path, @NotNull Class<T> typeOfModel) {
        return new ObjectConfigurationHandler<>(path, typeOfModel);
    }

    public static <T> @NotNull ObjectConfigurationHandler<T> ofModule(@NotNull String other, @NotNull Class<T> typeOfModel) {
        Path resolvedModuleFile = ReflectionUtil.computeModuleConfigPath(typeOfModel).resolve(other);
        return ofPath(resolvedModuleFile, typeOfModel);
    }

    @SneakyThrows(Exception.class)
    @Override
    protected T getDefaultModel() {
        return typeOfModel.getDeclaredConstructor().newInstance();
    }

}

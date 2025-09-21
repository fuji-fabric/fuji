package mod.fuji.core.config.handler.impl;

import com.google.gson.JsonObject;
import mod.fuji.core.auxiliary.ReflectionUtil;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.validator.RemoveNullElementsInJsonArrayValidator;
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
        Path moduleFilePath = ReflectionUtil.computeModuleConfigPath(typeOfModel).resolve(other);
        return ofPath(moduleFilePath, typeOfModel);
    }

    @SneakyThrows(Exception.class)
    @Override
    protected T makeDefaultModel() {
        return typeOfModel.getDeclaredConstructor().newInstance();
    }

    @Override
    protected void validateModel(@NotNull JsonObject dataTree, @NotNull JsonObject schemaTree) {
        super.validateModel(dataTree, schemaTree);
        RemoveNullElementsInJsonArrayValidator.validate(dataTree);
    }
}

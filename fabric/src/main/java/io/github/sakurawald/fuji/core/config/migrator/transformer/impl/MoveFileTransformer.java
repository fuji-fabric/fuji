package io.github.sakurawald.fuji.core.config.migrator.transformer.impl;

import io.github.sakurawald.fuji.core.config.migrator.transformer.abst.ConfigurationTransformer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class MoveFileTransformer extends ConfigurationTransformer {

    @NotNull Path sourceFile;
    @NotNull Path destinationFile;

    @SneakyThrows(IOException.class)
    @Override
    public void apply() {
        if (Files.notExists(sourceFile) || Files.exists(destinationFile)) return;

        logOperation("Move the file: sourceFile = {}, destinationFile = {}", sourceFile, destinationFile);
        Files.createDirectories(this.destinationFile.getParent());
        Files.move(sourceFile, destinationFile);
    }

}

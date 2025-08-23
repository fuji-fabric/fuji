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

    @SuppressWarnings("RedundantIfStatement")
    @Override
    protected boolean canApply() {
        if (Files.notExists(sourceFile)) return false;
        if (Files.exists(destinationFile)) return false;

        return true;
    }

    @SneakyThrows(IOException.class)
    @Override
    protected void apply() {
        logOperation("Move file: sourceFile = {}, destinationFile = {}", sourceFile, destinationFile);
        Files.createDirectories(this.destinationFile.getParent());
        Files.move(sourceFile, destinationFile);
    }

}

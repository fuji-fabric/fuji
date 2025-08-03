package io.github.sakurawald.fuji.core.config.transformer.impl;

import io.github.sakurawald.fuji.core.config.transformer.abst.ConfigurationTransformer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class MoveFileTransformer extends ConfigurationTransformer {

    @NotNull Path destinationFile;

    @SneakyThrows(IOException.class)
    @Override
    public void apply() {
        Path sourceFile = getTargetFilePath();
        if (Files.notExists(sourceFile) || Files.exists(destinationFile)) return;

        logOperation("Move the file to {}", destinationFile);
        Files.createDirectories(this.destinationFile.getParent());
        Files.move(sourceFile, destinationFile);
    }

}

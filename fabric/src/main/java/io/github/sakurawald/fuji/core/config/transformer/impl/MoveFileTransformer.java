package io.github.sakurawald.fuji.core.config.transformer.impl;

import io.github.sakurawald.fuji.core.config.transformer.abst.JsonConfigurationTransformer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@AllArgsConstructor
public class MoveFileTransformer extends JsonConfigurationTransformer {

    Path sourceFile;
    Path destinationDirectory;

    @SneakyThrows(IOException.class)
    @Override
    public void apply() {
        destinationDirectory = destinationDirectory.resolve(this.getTargetFilePath().toFile().getName());

        if (Files.notExists(this.sourceFile) || Files.exists(destinationDirectory)) return;

        Files.createDirectories(this.destinationDirectory.getParent());
        logOperation("move the file to {}", destinationDirectory);
        Files.move(sourceFile, destinationDirectory);
    }

}

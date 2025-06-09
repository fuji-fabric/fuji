package io.github.sakurawald.core.config.transformer.impl;

import io.github.sakurawald.core.config.transformer.abst.ConfigurationTransformer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@AllArgsConstructor
public class MoveFileTransformer extends ConfigurationTransformer {

    Path sourceFile;
    Path destinationDirectory;

    @SneakyThrows(IOException.class)
    @Override
    public void apply() {
        destinationDirectory = destinationDirectory.resolve(this.getPath().toFile().getName());

        if (Files.notExists(this.sourceFile) || Files.exists(destinationDirectory)) return;

        Files.createDirectories(this.destinationDirectory.getParent());
        logConsole("move the file to {}", destinationDirectory);
        Files.move(sourceFile, destinationDirectory);
    }

}

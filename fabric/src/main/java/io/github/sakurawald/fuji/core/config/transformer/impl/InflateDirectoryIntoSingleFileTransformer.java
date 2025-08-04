package io.github.sakurawald.fuji.core.config.transformer.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.sakurawald.fuji.core.auxiliary.JsonUtil;
import io.github.sakurawald.fuji.core.config.transformer.abst.ConfigurationTransformer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public abstract class InflateDirectoryIntoSingleFileTransformer extends ConfigurationTransformer {

    @SneakyThrows
    @Override
    public void apply() {
        var inputDirectoryPath = inputDirectoryPath();
        var outputFilePath = outputFilePath();

        /* Check if the input directory path exists. */
        if (!Files.exists(inputDirectoryPath)) {
            return;
        }

        /* List files in input directory path. */
        @Cleanup Stream<Path> inputFiles = Files.list(inputDirectoryPath);

        /* Make the output array to hold the inflated data. */
        JsonObject outputJson = new JsonObject();
        JsonArray outputArray = arrayMaker(outputJson);

        /* Apply the mapper for each input file. */
        inputFiles
            .toList()
            .forEach(inputFilePath -> {
                /* Append the mapped JsonObject. */
                String fileName = inputFilePath.getFileName().toString();
                JsonObject inputFileJson = JsonUtil.readJsonElement(inputFilePath).getAsJsonObject();
                JsonObject outputFileJson = mapper(fileName, inputFileJson);
                outputArray.add(outputFileJson);

                /* Delete the input file for that input JsonObject. */
                try {
                    Files.delete(inputFilePath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        /* Write the output file. */
        if (!Files.exists(outputFilePath)) {
            Files.createDirectories(outputFilePath.getParent());
            JsonUtil.writeJsonObject(outputJson, outputFilePath);
        }

        /* Delete the empty directory. */
        @Cleanup Stream<Path> $inputFiles = Files.list(inputDirectoryPath);
        if ($inputFiles.toList().isEmpty()) {
            Files.delete(inputDirectoryPath);
        }
    }

    @NotNull
    protected abstract Path inputDirectoryPath();

    @NotNull
    protected abstract Path outputFilePath();

    @NotNull
    protected abstract JsonArray arrayMaker(@NotNull JsonObject root);

    @NotNull
    protected abstract JsonObject mapper(@NotNull String fileName, @NotNull JsonObject inputJson);
}

package mod.fuji.core.config.migrator.transformer.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mod.fuji.core.auxiliary.ExceptionUtil;
import mod.fuji.core.auxiliary.IOUtil;
import mod.fuji.core.auxiliary.JsonUtil;
import mod.fuji.core.config.migrator.transformer.abst.ConfigurationTransformer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class InflateDirectoryIntoSingleFileTransformer extends ConfigurationTransformer {

    final Path inputDirectoryPath;
    final Path outputFilePath;
    final Function<JsonObject, JsonArray> outputArrayProvider;
    final BiFunction<String, JsonObject, JsonObject> mapper;

    @SuppressWarnings("RedundantIfStatement")
    @Override
    protected boolean canApply() {
        /* Check if the input directory path exists. */
        if (!Files.exists(this.inputDirectoryPath)) return false;
        if (Files.exists(this.outputFilePath)) return false;

        return true;
    }

    @SneakyThrows(IOException.class)
    @Override
    protected void apply() {
        /* List files in input directory path. */
        @Cleanup Stream<Path> inputFiles = Files.list(this.inputDirectoryPath);

        /* Make the output array to hold the inflated data. */
        JsonObject outputJson = new JsonObject();
        JsonArray outputArray = this.outputArrayProvider.apply(outputJson);

        /* Apply the mapper for each input file. */
        inputFiles
            .toList()
            .forEach(inputFilePath -> {
                /* Append the mapped JsonObject. */
                String inputFileName = inputFilePath.getFileName().toString();
                JsonObject inputFileJson = JsonUtil.readJsonFile(inputFilePath).getAsJsonObject();
                JsonObject outputFileJson = this.mapper.apply(inputFileName, inputFileJson);
                outputArray.add(outputFileJson);

                /* Delete the input file for that input JsonObject. */
                try {
                    Files.delete(inputFilePath);
                } catch (IOException e) {
                    throw ExceptionUtil.makeReThrownException(e);
                }
            });

        /* Write the output file. */
        JsonUtil.writeJsonObject(outputJson, this.outputFilePath);

        /* Delete the empty directory. */
        IOUtil.deleteDirectoryIfEmpty(this.inputDirectoryPath);
    }

}

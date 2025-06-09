package io.github.sakurawald.core.config.transformer.abst;

import com.jayway.jsonpath.DocumentContext;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("LombokGetterMayBeUsed")
public abstract class ConfigurationTransformer {

    @Getter
    private Path path;

    public void configure(Path path) {
        this.path = path;
    }

    @SneakyThrows(IOException.class)
    public DocumentContext makeDocumentContext() {
        return BaseConfigurationHandler.getJsonPathParser().parse(this.path.toFile());
    }

    public abstract void apply();

    public boolean exists(DocumentContext context, String jsonPath) {
        try {
            context.read(jsonPath);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean notExists(DocumentContext context, String jsonPath) {
        return !exists(context, jsonPath);
    }

    public void logConsole(String message, Object... args) {
        Object[] fullArgs = new Object[args.length + 1];
        fullArgs[0] = this.path;
        System.arraycopy(args, 0, fullArgs, 1, args.length);

        LogUtil.warn("apply the transformer associated with the file `{}`\n => " + message, fullArgs);
    }

    @SneakyThrows(IOException.class)
    public void writeStorage(DocumentContext context) {
        this.logConsole("write storage");
        String json = BaseConfigurationHandler.getGson().toJson(context.json());
        Files.writeString(this.path, json);
    }

    public Object read(DocumentContext context, String jsonPath) {
        return context.read(jsonPath);
    }

    public void set(DocumentContext context, String jsonPath, Object newValue) {
        this.logConsole("set key `{}`: {}", jsonPath, newValue);
        context.set(jsonPath, newValue);
    }

}

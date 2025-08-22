package io.github.sakurawald.fuji.module.initializer.command_meta.json;

import com.jayway.jsonpath.DocumentContext;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.config.mapper.GsonMapper;
import io.github.sakurawald.fuji.core.config.parser.JsonPathParser;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_meta.json.command.argument.wrapper.JsonValueType;
import lombok.SneakyThrows;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;

@Document(id = 1751823967217L, value = """
    Provides `/json` command.
    A powerful and unified tool to edit json file.
    """)
@ColorBox(id = 1751969995780L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Read a json key.
    Issue: `/json read "config/fuji/config.json" "$.core.debug"`

    ◉ List json keys.
    Issue: `/json read "config/fuji/config.json" "$.modules.keys()"`

    ◉ Set the value of a json key.
    Issue: `/json write "config/fuji/config.json" "$.core.debug.log_debug_messages" BOOLEAN true`
    """)
@ColorBox(id = 1751970168900L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    Read the detailed document for `Json Path`:
    See https://goessner.net/articles/JsonPath/
    """)



@CommandNode("json")
@CommandRequirement(level = 4)
public class JsonInitializer extends ModuleInitializer {

    @SneakyThrows(IOException.class)
    private static void operateJson(String filePath, BiFunction<DocumentContext, Path, Boolean> function) {
        Path path = Path.of(filePath);
        DocumentContext documentContext = JsonPathParser.getJsonPathParser().parse(path.toFile());
        Boolean destructiveFlag = function.apply(documentContext, path);

        if (destructiveFlag) {
            String json = GsonMapper.getGson().toJson(documentContext.json());
            try {
                Files.writeString(path, json);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @CommandNode("read")
    private static int $read(@CommandSource CommandContext<ServerCommandSource> ctx, String filePath, String jsonPath) {
        operateJson(filePath, (documentContext, path) -> {
            Object read = documentContext.read(jsonPath);
            TextHelper.sendMessageByText(ctx.getSource(), Text.literal(read.toString()));
            return false;
        });
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("write")
    private static int $write(@CommandSource CommandContext<ServerCommandSource> ctx, String filePath, String jsonPath, JsonValueType valueType, GreedyString value) {
        operateJson(filePath, (documentContext, path) -> {
            Object obj = valueType.parse(value.getValue());
            documentContext.set(jsonPath, obj);
            return true;
        });
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("delete")
    private static int $delete(@CommandSource CommandContext<ServerCommandSource> ctx, String filePath, String jsonPath) {
        operateJson(filePath, (documentContext, path) -> {
            documentContext.delete(jsonPath);
            return true;
        });
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("put")
    private static int $put(@CommandSource CommandContext<ServerCommandSource> ctx, String filePath, String jsonPath, String jsonKey, JsonValueType valueType, GreedyString value) {
        operateJson(filePath, (documentContext, path) -> {
            Object obj = valueType.parse(value.getValue());
            documentContext.put(jsonPath, jsonKey, obj);
            return true;
        });
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("renameKey")
    private static int $renameKey(@CommandSource CommandContext<ServerCommandSource> ctx, String filePath, String jsonPath, String oldJsonKey, String newJsonKey) {
        operateJson(filePath, (documentContext, path) -> {
            documentContext.renameKey(jsonPath, oldJsonKey, newJsonKey);
            return true;
        });
        return CommandHelper.Return.SUCCESS;
    }
}

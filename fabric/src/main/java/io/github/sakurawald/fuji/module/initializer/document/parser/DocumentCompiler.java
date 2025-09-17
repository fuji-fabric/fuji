package io.github.sakurawald.fuji.module.initializer.document.parser;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class DocumentCompiler {

    public static @NotNull String compile(@NotNull String input) {
        return Arrays
            .stream(input.split("\n"))
            .map(DocumentCompiler::compileLine)
            .collect(Collectors.joining("\n\n"));
    }

    private static @NotNull String compileLine(@NotNull String input) {
        String output = input.replaceAll("(?<!\\\\)<(.*?)>(?!\\\\)", "\\\\<$1\\\\>");
//        String output = input.replaceAll("(?<!\\\\)<(.*?)>(?!\\\\)", "<span style={{color: \"blue\"}}>$1</span>");
        output = output.replaceAll("<=", "\\\\<=");
        return output;
    }
}

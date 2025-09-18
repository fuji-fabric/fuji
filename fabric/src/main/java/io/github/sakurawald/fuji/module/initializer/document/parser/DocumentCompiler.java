package io.github.sakurawald.fuji.module.initializer.document.parser;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class DocumentCompiler {

    public static @NotNull String compile(@NotNull String input) {
        return Arrays
            .stream(input.split("\n"))
            .map(DocumentCompiler::compileLine)
            .collect(Collectors.joining("\n"));
    }

    private static @NotNull String compileLine(@NotNull String input) {
        String output = input;

        /* Stripe the named color tags. */
        for (String namedColor : TextHelper.Formatter.NAMED_STYLE_TAGS) {
            output = output.replaceAll("<\\/?" + namedColor + ">","");
        }

        /* Stripe the hex color tags. */
        output = output.replaceAll("<\\/?#[a-zA-Z0-9_]+>", "");

        output = output.replaceAll("<=", "\\\\<=");
        return output;
    }
}

package io.github.sakurawald.fuji.module.initializer.document.builder;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.auxiliary.DocumentUtil;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ModuleColorBoxesDocumentBuilder extends DocumentBuilder {

    @Override
    public void build(@NotNull DocumentBuilderContext documentBuilderContext) {
        List<ColorBox> colorBoxes = DocumentUtil.getColorBoxes(documentBuilderContext.getModulePathString());
        if (!colorBoxes.isEmpty()) {
            StringBuilder documentBuilder = documentBuilderContext.getDocumentBuilder();
            documentBuilder
                .append("## Color Boxes")
                .append(System.lineSeparator()).append(System.lineSeparator());

            colorBoxes.forEach(it -> buildColorBox(documentBuilderContext, it));
        }
    }

    private void buildColorBox(@NotNull DocumentBuilderContext documentBuilderContext, @NotNull ColorBox colorBox) {
        documentBuilderContext
            .getDocumentBuilder()
            .append(":::%s".formatted(toAdmonitionName(colorBox))).append(System.lineSeparator()).append(System.lineSeparator())
            .append(DocumentUtil.duplicateLineSeparatorCharacter(colorBox.value()))
            .append(":::").append(System.lineSeparator()).append(System.lineSeparator());
    }

    private @NotNull String toAdmonitionName(@NotNull ColorBox colorBox) {
        if (colorBox.color() == ColorBox.ColorBoxTypes.DANGER) return "colorbox-danger";
        if (colorBox.color() == ColorBox.ColorBoxTypes.WARNING) return "colorbox-warning";
        if (colorBox.color() == ColorBox.ColorBoxTypes.NOTE) return "colorbox-note";
        if (colorBox.color() == ColorBox.ColorBoxTypes.TIP) return "colorbox-tip";
        if (colorBox.color() == ColorBox.ColorBoxTypes.EXAMPLE) return "colorbox-example";
        throw new IllegalArgumentException("Unknown color: " + colorBox.color());
    }
}

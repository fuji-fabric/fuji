package io.github.sakurawald.fuji.module.initializer.document.builder;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.auxiliary.DocumentUtil;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ModuleColorBoxDocumentBuilder extends DocumentBuilder {

    @Override
    public void build(@NotNull DocumentBuilderContext documentBuilderContext) {
        List<ColorBox> colorBoxes = DocumentUtil.getColorBoxes(documentBuilderContext.getModulePathString());
        if (!colorBoxes.isEmpty()) {
            StringBuilder documentBuilder = documentBuilderContext.getDocumentBuilder();
            documentBuilder
                .append("## Color Boxes")
                .append(System.lineSeparator());

            colorBoxes.forEach(it -> buildColorBox(documentBuilderContext, it));
        }
    }

    private void buildColorBox(@NotNull DocumentBuilderContext documentBuilderContext, @NotNull ColorBox colorBox) {
        documentBuilderContext
            .getDocumentBuilder()
            .append("### %s".formatted(colorBox.color().name()))
            .append(System.lineSeparator())
            .append(colorBox.value());
    }
}

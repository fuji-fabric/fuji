package mod.fuji.module.initializer.document.formatter;

import org.jetbrains.annotations.NotNull;

public class FileNameFormatter {
    private int documentFileIndex;

    public void resetFileIndex() {
        this.documentFileIndex = 0;
    }

    public @NotNull String formatFileName(@NotNull String fileName) {
        String result = "%03d".formatted(this.documentFileIndex) + "-" + fileName;
        this.documentFileIndex++;
        return result;
    }
}

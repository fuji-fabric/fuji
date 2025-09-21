package mod.fuji.core.gui.component.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EditSignGui extends InputSignGui {

    public EditSignGui(@NotNull ServerPlayerEntity player, @Nullable String defaultValue) {
        super(player, null);

        /* Set the default value. */
        if (defaultValue != null) {
            List<String> lines = splitLines(defaultValue);
            for (int i = 0; i < lines.size(); i++) {
                if (i > 3) break;
                String line = lines.get(i);
                this.setLine(i, Text.literal(line));
            }
        }
    }

    private static @NotNull List<String> splitLines(@NotNull String string) {
        List<String> lines = new ArrayList<>();

        final int maxLineCharacters = 22;
        int maxIndexes = string.length() / maxLineCharacters;
        for (int i = 0; i <= maxIndexes; i++) {
            int begin = i * maxLineCharacters;
            int end = begin + maxLineCharacters;
            end = Math.min(end, string.length());

            String line = string.substring(begin, end);
            lines.add(line);
        }

        return lines;
    }

}

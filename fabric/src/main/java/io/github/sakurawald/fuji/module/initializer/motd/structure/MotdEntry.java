package io.github.sakurawald.fuji.module.initializer.motd.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MotdEntry {

    @Document(id = 1751826859471L, value = """
        The `text` used for this `motd` entry.
        """)
    @NotNull
    String text;

    @Document(id = 1751826861498L, value = """
        The `icon` used for this `motd` entry.

        The `icon` must be 64x64 png image.
        """)
    @Nullable
    String icon;

}

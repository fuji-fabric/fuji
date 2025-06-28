package io.github.sakurawald.fuji.module.initializer.motd.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public class MotdEntry {

    @Document("""
        The `text` used for this `motd` entry.
        """)
    @NotNull String text;

    @Document("""
        The `icon` used for this `motd` entry.

        The `icon` must be 64x64 png image.
        """)
    @Nullable String icon;

}

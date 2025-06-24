package io.github.sakurawald.module.initializer.color.sign.config.model;

import io.github.sakurawald.core.annotation.Document;

public class ColorSignConfigModel {
    @Document("""
        By default, any player can use `all style tags`.
        Enable this option requires the player to has `corresponding permission` to use that `style tag`.

        For example, to use `<red>` tag, requires `fuji.style.sign.red` permission.
        """)
    public boolean requires_corresponding_permission_to_use_style_tag = false;
}

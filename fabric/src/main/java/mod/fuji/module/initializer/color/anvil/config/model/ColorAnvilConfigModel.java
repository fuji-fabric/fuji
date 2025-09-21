package mod.fuji.module.initializer.color.anvil.config.model;

import mod.fuji.core.document.annotation.Document;

public class ColorAnvilConfigModel {
    @Document(id = 1751824937869L, value = """
        By default, any player can use `all style tags`.
        Enable this option requires the player to has `corresponding permission` to use that `style tag`.

        For example, to use `<red>` tag, requires `fuji.style.anvil.red` permission.
        """)
    public boolean requires_corresponding_permission_to_use_style_tag = false;
}

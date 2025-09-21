package mod.fuji.module.initializer.whitelist;

import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;

@Document(id = 1751981390616L, value = """
    This module makes the `vanilla whitelist system` only compares the `username`, and `ignore the UUID`.
    """)
@ColorBox(id = 1751981431675L, color = ColorBox.ColorBoxTypes.WARNING, value = """
    ◉ Only enable this module in offline server.
    If you are hosting an online-mode server, then you didn't need to enable this module.

    ◉ What is the difference?
    For `online-mode` server, the `UUID` of a player is determined, and always identical.
    For `offline-mode` server, the `UUID` of a player is changed by the `authenticate method` of the player, and affected by the `user cache` in server side.
    """)
public class WhitelistInitializer extends ModuleInitializer {
}

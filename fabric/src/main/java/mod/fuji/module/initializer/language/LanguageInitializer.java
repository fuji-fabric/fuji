package mod.fuji.module.initializer.language;

import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;

@Document(id = 1751826368714L, value = """
    This module enables respect for the `client-side language option` whenever possible.
    """)
@ColorBox(id = 1751977937481L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ What is client-side language?
    After `Minecraft 1.20.1`, the `client` will send its `client side language option` to the `server`.
    So the `server` can respect the `client-side language` if possible.

    ◉ What is the difference?
    When disable this module: fuji will always use `default_language` for all players.
    When enable this module: fuji will try to respect the player's `client-side language option` if possible.

    That's to say, if you enable this module, then fuji will support `multiple language` at the same time.
    Fuji will use `different languages` for different players. (Respect the client-side language option)
    """)
public class LanguageInitializer extends ModuleInitializer {

}

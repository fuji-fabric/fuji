package io.github.sakurawald.fuji.module.initializer.language;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;

@Document(id = 1751826368714L, value = """
    Respect the `client-side` language option, if possible.
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

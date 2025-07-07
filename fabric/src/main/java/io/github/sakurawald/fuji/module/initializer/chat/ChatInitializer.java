package io.github.sakurawald.fuji.module.initializer.chat;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;

@ColorBox(id = 1751870564305L, value = """
    All sub-modules of `chat` module are designed to work with `other mods`.
    Especially, provides the first support to work with `Styled Chat` mod.
    For other `chat-related` mods, you can try and test the compatibility.
    It's likely it will work.
    """)
@ColorBox(id = 1751870566083L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    The `placeholder` module provides many `extra placeholders` for `Text Placeholder API`.
    You can enable that module to get more placeholders.

    Text Placeholder API - default placeholders
    https://placeholders.pb4.eu/user/default-placeholders/

    """)
@ColorBox(id = 1751870567628L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    The `luckperms` mod provides the `prefix` and `suffix` for players.
    Which can be used as the `player title`.

    See: https://luckperms.net/wiki/Prefixes,-Suffixes-&-Meta
    """)
public class ChatInitializer extends ModuleInitializer {

}

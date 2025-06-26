package io.github.sakurawald.module.initializer.chat;

import io.github.sakurawald.core.structure.descriptor.annotation.ColorBox;
import io.github.sakurawald.module.initializer.ModuleInitializer;

@ColorBox("""
    All sub-modules of `chat` module are designed to work with `other mods`.
    Especially, provides the first support to work with `Styled Chat` mod.
    For other `chat-related` mods, you can try and test the compatibility.
    It's likely it will work.
    """)
@ColorBox(color = ColorBox.ColorBlockTypes.TIPS, value = """
   Text Placeholder API - default placeholders
   https://placeholders.pb4.eu/user/default-placeholders/
   """)
public class ChatInitializer extends ModuleInitializer {

}

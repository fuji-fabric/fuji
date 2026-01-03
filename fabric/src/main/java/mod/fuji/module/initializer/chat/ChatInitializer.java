package mod.fuji.module.initializer.chat;

import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.module.initializer.ModuleInitializer;

@Document(id = 1767428586044L, value = """
    This module provides functions for vanilla Minecraft's `chat` system.
    """)
@ColorBox(id = 1751870564305L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ The compatibility issue with other `chat-related` mods.
    There is not a standard API to interact with the vanilla Minecraft's chat system.
    It makes the compatibility issue between `chat-related mods` harder to resolve.

    I try my best to write the code in the most compatible way.
    If any of the chat module doesn't work, feel free to open an issue.
    """)
@TestCase(action = "Enable the `online-mode` in server.properties file", targets = "All of chat-related modules should not break the Mojang's chat signature.")
@TestCase(action = "Test the chat-related modules with `Styled Chat` mod.", targets = "It should work fine with other mods.")
public class ChatInitializer extends ModuleInitializer {

}

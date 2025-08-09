package io.github.sakurawald.fuji.module.initializer.disabler.move_wrongly_disabler;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;

@Document(id = 1751975598706L, value = """
    This module disables `player moved wrongly` and `vehicle moved wrongly` checkers.
    """)
@ColorBox(id = 1751975648480L, color = ColorBox.ColorBoxTypes.WARNING, value = """
    In vanilla Minecraft server, there is a checker to check if a player moves correctly.
    However, this checker usually makes false detection.
    And force setback the player, which makes the client-side feels the gameplay is lagged.
    """)
public class MoveWronglyDisablerInitializer extends ModuleInitializer {
}

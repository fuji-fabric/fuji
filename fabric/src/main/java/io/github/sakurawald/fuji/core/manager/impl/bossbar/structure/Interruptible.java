package io.github.sakurawald.fuji.core.manager.impl.bossbar.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.Data;

@Data
public class Interruptible {
    @Document(id = 1751823914000L, value = """
        Is this request interruptible?
        """)
    final boolean enable;

    @Document(id = 1751823921153L, value = """
        The max distance to interrupt this request.
        """)
    final double interruptDistance;

    @Document(id = 1751823933921L, value = """
        Interrupt this request when player damaged.
        """)
    final boolean interruptOnDamaged;

    @Document(id = 1751823938547L, value = """
        Interrupt this request if player in combat.
        """)
    final boolean interruptInCombat;

    public static Interruptible makeUninterruptible() {
        return new Interruptible(false, 2048, false, false);
    }
}

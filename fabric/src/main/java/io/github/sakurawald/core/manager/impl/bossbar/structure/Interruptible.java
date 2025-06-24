package io.github.sakurawald.core.manager.impl.bossbar.structure;

import io.github.sakurawald.core.annotation.Document;
import lombok.Data;

@Data
public class Interruptible {
    @Document("""
        Is this request interruptible?
        """)
    final boolean enable;

    @Document("""
        The max distance to interrupt this request.
        """)
    final double interruptDistance;

    @Document("""
        Interrupt this request when player damaged.
        """)
    final boolean interruptOnDamaged;

    @Document("""
        Interrupt this request if player in combat.
        """)
    final boolean interruptInCombat;

    public static Interruptible makeUninterruptible() {
        return new Interruptible(false, 2048, false, false);
    }
}

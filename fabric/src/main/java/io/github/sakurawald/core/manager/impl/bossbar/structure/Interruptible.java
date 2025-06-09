package io.github.sakurawald.core.manager.impl.bossbar.structure;

import lombok.Data;

@Data
public class Interruptible {
    final boolean enable;
    final double interruptDistance;
    final boolean interruptOnDamaged;
    final boolean interruptInCombat;

    public static Interruptible makeUninterruptible() {
        return new Interruptible(false, 2048, false, false);
    }
}

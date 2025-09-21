package mod.fuji.core.manager.impl.bossbar.structure;

import mod.fuji.core.document.annotation.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Interruptible {
    @Document(id = 1751823914000L, value = """
        Is this request interruptible?
        """)
    boolean enable;

    @Document(id = 1751823921153L, value = """
        The max distance to interrupt this request.
        """)
    double interruptDistance;

    @Document(id = 1751823933921L, value = """
        Interrupt this request when player damaged.
        """)
    boolean interruptOnDamaged;

    @Document(id = 1751823938547L, value = """
        Interrupt this request if player in combat.
        """)
    boolean interruptInCombat;

    public static Interruptible makeUninterruptible() {
        return new Interruptible(false, 2048, false, false);
    }
}

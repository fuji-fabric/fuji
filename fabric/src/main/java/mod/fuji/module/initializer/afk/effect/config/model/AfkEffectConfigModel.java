package mod.fuji.module.initializer.afk.effect.config.model;

import mod.fuji.core.document.annotation.Document;

public class AfkEffectConfigModel {

    @Document(id = 1751826184182L, value = """
        Should the `afk player` be invulnerable?
        """)
    public boolean invulnerable = true;

    @Document(id = 1751826187343L, value = """
        Should the `afk player` be targeted by `hostile entity`?
        """)
    public boolean targetable = false;

    @Document(id = 1751826191115L, value = """
        Should the `afk player` be moveable? (Like `pushed by piston` or `gravity`)
        """)
    public boolean moveable = false;
}

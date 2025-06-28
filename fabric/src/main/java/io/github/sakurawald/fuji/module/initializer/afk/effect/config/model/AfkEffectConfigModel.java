package io.github.sakurawald.fuji.module.initializer.afk.effect.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;

public class AfkEffectConfigModel {

    @Document("""
        Should the `afk player` be invulnerable?
        """)
    public boolean invulnerable = true;

    @Document("""
        Should the `afk player` be targeted by `hostile entity`?
        """)
    public boolean targetable = false;

    @Document("""
        Should the `afk player` be moveable? (Like `pushed by piston` or `gravity`)
        """)
    public boolean moveable = false;
}

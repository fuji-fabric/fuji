package io.github.sakurawald.module.initializer.afk.effect.config.model;

import io.github.sakurawald.core.annotation.Document;

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

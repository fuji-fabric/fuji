package io.github.sakurawald.core.command.argument.wrapper.impl;

import com.mojang.authlib.GameProfile;
import io.github.sakurawald.core.command.argument.wrapper.abst.SingularValue;

import java.util.Collection;

public class GameProfileCollection extends SingularValue<Collection<GameProfile>> {
    public GameProfileCollection(Collection<GameProfile> value) {
        super(value);
    }
}

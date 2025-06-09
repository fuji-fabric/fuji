package io.github.sakurawald.core.command.argument.wrapper.impl;

import io.github.sakurawald.core.command.argument.wrapper.abst.SingularValue;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;

public class PlayerCollection extends SingularValue<Collection<ServerPlayerEntity>> {
    public PlayerCollection(Collection<ServerPlayerEntity> value) {
        super(value);
    }
}

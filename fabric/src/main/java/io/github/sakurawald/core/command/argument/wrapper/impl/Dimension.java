package io.github.sakurawald.core.command.argument.wrapper.impl;

import io.github.sakurawald.core.command.argument.wrapper.abst.SingularValue;
import net.minecraft.server.world.ServerWorld;


public class Dimension extends SingularValue<ServerWorld> {

    public Dimension(ServerWorld value) {
        super(value);
    }
}

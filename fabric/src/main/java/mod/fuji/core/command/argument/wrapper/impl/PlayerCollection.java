package mod.fuji.core.command.argument.wrapper.impl;

import mod.fuji.core.command.argument.wrapper.abst.SingularValue;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class PlayerCollection extends SingularValue<Collection<ServerPlayer>> {
    public PlayerCollection(Collection<ServerPlayer> value) {
        super(value);
    }
}

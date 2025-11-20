package mod.fuji.core.command.argument.wrapper.impl;

import mod.fuji.core.command.argument.wrapper.abst.SingularValue;
import net.minecraft.server.level.ServerLevel;


public class Dimension extends SingularValue<ServerLevel> {

    public Dimension(ServerLevel value) {
        super(value);
    }
}

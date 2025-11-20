package mod.fuji.core.command.argument.wrapper.impl;

import mod.fuji.core.command.argument.wrapper.abst.SingularValue;

import java.util.Collection;

public class GameProfileCollection extends
    #if MC_VER < MC_1_21_9
    SingularValue<Collection<com.mojang.authlib.GameProfile>>
    #elif MC_VER >= MC_1_21_9
    SingularValue<Collection<net.minecraft.server.players.NameAndId>>
    #endif
{
    public GameProfileCollection(
        #if MC_VER < MC_1_21_9
        Collection<com.mojang.authlib.GameProfile> value
        #elif MC_VER >= MC_1_21_9
        Collection<net.minecraft.server.players.NameAndId> value
        #endif
    ) {
        super(value);
    }
}

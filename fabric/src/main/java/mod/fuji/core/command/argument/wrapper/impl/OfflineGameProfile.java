package mod.fuji.core.command.argument.wrapper.impl;

import com.mojang.authlib.GameProfile;
import mod.fuji.core.command.argument.wrapper.abst.SingularValue;

public class OfflineGameProfile extends SingularValue<GameProfile> {
    public OfflineGameProfile(GameProfile value) {
        super(value);
    }
}

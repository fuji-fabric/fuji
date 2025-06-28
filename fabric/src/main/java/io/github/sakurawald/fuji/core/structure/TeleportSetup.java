package io.github.sakurawald.fuji.core.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import lombok.Data;
import net.minecraft.server.world.ServerWorld;

@SuppressWarnings("unused")
@Data
public class TeleportSetup {
    @Document("""
        The `target dimension` of this setup.
        """)
    final String dimension;
    final int centerX;
    final int centerZ;
    @Document("""
        Use `circle` shape or `rectangle` shape for `rtp`?
        """)
    final boolean circle;
    final int minRange;
    final int maxRange;
    final int minY;
    final int maxY;

    @Document("""
        Max try times before aborting a `rtp`.
        """)
    final int maxTryTimes;

    public ServerWorld ofWorld() {
        return RegistryHelper.ofServerWorld(this.dimension);
    }

}


package io.github.sakurawald.fuji.core.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import lombok.Data;
import net.minecraft.server.world.ServerWorld;

@Data
public class TeleportSetup {
    @Document("""
        The `target dimension` of this setup.
        """)
    final String dimension;

    @Document("""
        The `center x` used as the `origin` for `rtp`.
        """)
    final int centerX;

    @Document("""
        See `centerX`.
        """)
    final int centerZ;

    @Document("""
        Use `circle` shape or `rectangle` shape for `rtp`?
        """)
    final boolean circle;

    @Document("""
        The `relative x/z distance` is ranged `(minRange, maxRange]`.
        """)
    final int minRange;

    @Document("""
        See `minRange`.
        """)
    final int maxRange;

    @Document("""
        The `relative y distance` is ranged `(minY, maxY]`.
        """)
    final int minY;

    @Document("""
        See `minY`.
        """)
    final int maxY;

    @Document("""
        Max try times before aborting a `rtp` request.
        """)
    final int maxTryTimes;

    public ServerWorld toDimension() {
        return RegistryHelper.ofServerWorld(this.dimension);
    }

}


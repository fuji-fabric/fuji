package io.github.sakurawald.fuji.core.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import lombok.Data;
import net.minecraft.server.world.ServerWorld;

@Data
public class TeleportSetup {
    @Document(id = 1751823841574L, value = """
        The `target dimension` of this setup.
        """)
    final String dimension;

    @Document(id = 1751823848934L, value = """
        The `center x` used as the `origin` for `rtp`.
        """)
    final int centerX;

    @Document(id = 1751823859284L, value = """
        See `centerX`.
        """)
    final int centerZ;

    @Document(id = 1751823867105L, value = """
        Use `circle` shape or `rectangle` shape for `rtp`?
        """)
    final boolean circle;

    @Document(id = 1751823872674L, value = """
        The `relative x/z distance` is ranged `(minRange, maxRange]`.
        """)
    final int minRange;

    @Document(id = 1751823877287L, value = """
        See `minRange`.
        """)
    final int maxRange;

    @Document(id = 1751823883726L, value = """
        The `relative y distance` is ranged `(minY, maxY]`.
        """)
    final int minY;

    @Document(id = 1751823889193L, value = """
        See `minY`.
        """)
    final int maxY;

    @Document(id = 1751823896496L, value = """
        Max try times before aborting a `rtp` request.
        """)
    final int maxTryTimes;

    public ServerWorld toDimension() {
        return RegistryHelper.getServerWorld(this.dimension);
    }

}


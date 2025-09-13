package io.github.sakurawald.fuji.core.service.random_teleport.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.server.world.ServerWorld;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RandomTeleportSettings {
    @Document(id = 1751823841574L, value = """
        The `target dimension` of this setup.
        """)
    String dimension;

    @Document(id = 1751823848934L, value = """
        The `center x` used as the `origin` for `rtp`.
        """)
    int centerX;

    @Document(id = 1751823859284L, value = """
        See `centerX`.
        """)
    int centerZ;

    @Document(id = 1751823867105L, value = """
        Use `circle` shape or `rectangle` shape for `rtp`?
        """)
    boolean circle;

    @Document(id = 1751823872674L, value = """
        The `relative x/z distance` is ranged `(minRange, maxRange]`.
        """)
    int minRange;

    @Document(id = 1751823877287L, value = """
        See `minRange`.
        """)
    int maxRange;

    @Document(id = 1751823883726L, value = """
        The `relative y distance` is ranged `(minY, maxY]`.
        """)
    int minY;

    @Document(id = 1751823889193L, value = """
        See `minY`.
        """)
    int maxY;

    @Document(id = 1751823896496L, value = """
        Max try times before aborting a `rtp` request.
        """)
    int maxTryTimes;

    public ServerWorld toDimension() {
        return WorldHelper.getWorldOrThrow(this.dimension);
    }

}


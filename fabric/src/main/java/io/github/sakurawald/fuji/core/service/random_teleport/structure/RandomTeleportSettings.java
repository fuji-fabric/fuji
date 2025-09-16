package io.github.sakurawald.fuji.core.service.random_teleport.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.service.random_teleport.filter.PositionFilter;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RandomTeleportSettings {

    boolean enable = true;

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

    int asyncChunkLoadingTimeoutTicks = 20 * 10;

    int chunkInhabitedTimeLowerThanTicks = Integer.MAX_VALUE;

    Biomes biomes = new Biomes();

    @Data
    @NoArgsConstructor
    public static class Biomes {
        Set<String> skip = new HashSet<>() {
            {
                this.add("minecraft:ocean");
                this.add("minecraft:deep_ocean");
                this.add("minecraft:warm_ocean");
                this.add("minecraft:lukewarm_ocean");
                this.add("minecraft:deep_lukewarm_ocean");
                this.add("minecraft:cold_ocean");
                this.add("minecraft:deep_cold_ocean");
                this.add("minecraft:frozen_ocean");
                this.add("minecraft:deep_frozen_ocean");
            }
        };
    }

    Blocks blocks = new Blocks();

    @Data
    @NoArgsConstructor
    public static class Blocks {
        Set<String> skip = new HashSet<>() {
            {
                PositionFilter.KNOWN_DANGEROUS_BLOCKS
                    .stream()
                    .map(RegistryHelper::getIdAsString)
                    .forEach(this::add);
            }
        };
    }

}


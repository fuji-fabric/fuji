package mod.fuji.module.initializer.works.structure;

import mod.fuji.module.initializer.works.structure.work.abst.Work;
import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class WorksBinding {

    public static final ConcurrentHashMap<BlockPos, Set<Work>> BLOCK_POS_2_WORKS = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<Integer, Set<Work>> ENTITY_2_WORKS = new ConcurrentHashMap<>();

    public static void bind(BlockPos blockPos, Work work) {
        BLOCK_POS_2_WORKS
            .computeIfAbsent(blockPos, k -> new HashSet<>())
            .add(work);
    }

    public static void bind(Integer entityID, Work work) {
        ENTITY_2_WORKS
            .computeIfAbsent(entityID, k -> new HashSet<>())
            .add(work);
    }

    public static void unbind(Work work) {
        /* Remove other objects bound to this work */
        BLOCK_POS_2_WORKS
            .values()
            .removeIf(works -> works.remove(work));

        ENTITY_2_WORKS
            .values()
            .removeIf(works -> works.remove(work));

        /* Do clean up to remove empty set. */
        BLOCK_POS_2_WORKS
            .entrySet()
            .removeIf(entry -> entry.getValue().isEmpty());

        ENTITY_2_WORKS
            .entrySet()
            .removeIf(entry -> entry.getValue().isEmpty());
    }
}

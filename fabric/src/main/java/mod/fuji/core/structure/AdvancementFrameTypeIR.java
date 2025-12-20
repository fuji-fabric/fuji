package mod.fuji.core.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AdvancementFrameTypeIR {
    TASK(
        #if MC_VER <= MC_1_20_2
        net.minecraft.advancements.FrameType.TASK
        #elif MC_VER > MC_1_20_2
        net.minecraft.advancements.AdvancementType.TASK
        #endif
    ),
    CHALLENGE(
        #if MC_VER <= MC_1_20_2
        net.minecraft.advancements.FrameType.CHALLENGE
        #elif MC_VER > MC_1_20_2
        net.minecraft.advancements.AdvancementType.CHALLENGE
        #endif
    ),
    GOAL(
        #if MC_VER <= MC_1_20_2
        net.minecraft.advancements.FrameType.GOAL
        #elif MC_VER > MC_1_20_2
        net.minecraft.advancements.AdvancementType.GOAL
        #endif
    );

    #if MC_VER <= MC_1_20_2
    final net.minecraft.advancements.FrameType type;
    #elif MC_VER > MC_1_20_2
    final net.minecraft.advancements.AdvancementType type;
    #endif

}

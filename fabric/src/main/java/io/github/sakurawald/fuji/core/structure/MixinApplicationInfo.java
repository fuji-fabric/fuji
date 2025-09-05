package io.github.sakurawald.fuji.core.structure;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.spongepowered.asm.mixin.MixinEnvironment;

@Data
@AllArgsConstructor
public class MixinApplicationInfo {
    String targetClassName;
    String mixinClassName;
    boolean applied;
    MixinEnvironment.Phase phase;
    Integer priority;
}

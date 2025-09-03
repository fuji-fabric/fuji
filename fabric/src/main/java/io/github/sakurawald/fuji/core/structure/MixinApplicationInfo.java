package io.github.sakurawald.fuji.core.structure;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MixinApplicationInfo {
    String targetClassName;
    String mixinClassName;
    boolean applied;
}

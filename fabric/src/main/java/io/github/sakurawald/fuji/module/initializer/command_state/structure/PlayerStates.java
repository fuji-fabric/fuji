package io.github.sakurawald.fuji.module.initializer.command_state.structure;

import io.github.sakurawald.fuji.core.manager.impl.cache.structure.Cache;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlayerStates {

    ConcurrentHashMap<String, Cache<Boolean>> stateMap = new ConcurrentHashMap<>();

}

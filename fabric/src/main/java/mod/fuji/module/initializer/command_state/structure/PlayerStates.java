package mod.fuji.module.initializer.command_state.structure;

import mod.fuji.core.manager.impl.cache.structure.Cache;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlayerStates {

    ConcurrentHashMap<String, Cache<Boolean>> stateMap = new ConcurrentHashMap<>();

}

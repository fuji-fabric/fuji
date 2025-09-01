package io.github.sakurawald.fuji.module.initializer.command_state.config.model;

import io.github.sakurawald.fuji.module.initializer.command_state.structure.PlayerStates;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommandStateDataModel {

    ConcurrentHashMap<String, PlayerStates> playerStatesMap = new ConcurrentHashMap<>();

}

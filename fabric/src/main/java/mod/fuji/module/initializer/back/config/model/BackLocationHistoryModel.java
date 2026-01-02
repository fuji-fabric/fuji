package mod.fuji.module.initializer.back.config.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import mod.fuji.core.config.mapper.structure.PlayerKey;
import mod.fuji.module.initializer.back.structure.LocationHistory;

import java.util.concurrent.ConcurrentHashMap;

@Data
@NoArgsConstructor
public class BackLocationHistoryModel {
    ConcurrentHashMap<PlayerKey, LocationHistory> player2history = new ConcurrentHashMap<>();
}

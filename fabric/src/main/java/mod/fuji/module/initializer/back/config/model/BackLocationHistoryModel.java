package mod.fuji.module.initializer.back.config.model;

import mod.fuji.core.config.mapper.structure.PlayerKey;
import mod.fuji.module.initializer.back.structure.LocationHistory;

import java.util.concurrent.ConcurrentHashMap;

public class BackLocationHistoryModel {
    public ConcurrentHashMap<PlayerKey, LocationHistory> player2history = new ConcurrentHashMap<>();
}

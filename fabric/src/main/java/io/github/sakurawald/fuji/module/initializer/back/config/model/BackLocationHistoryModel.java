package io.github.sakurawald.fuji.module.initializer.back.config.model;

import io.github.sakurawald.fuji.module.initializer.back.structure.LocationHistory;

import java.util.concurrent.ConcurrentHashMap;

public class BackLocationHistoryModel {
    public ConcurrentHashMap<String, LocationHistory> player2history = new ConcurrentHashMap<>();
}

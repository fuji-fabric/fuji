package io.github.sakurawald.fuji.module.initializer.nametag.config.model;

import io.github.sakurawald.fuji.module.initializer.nametag.structure.NametagPlayerPreferences;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NametagDataModel {

    ConcurrentHashMap<String, NametagPlayerPreferences> preferences = new ConcurrentHashMap<>();

}

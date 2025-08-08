package io.github.sakurawald.fuji.module.initializer.warning.structure;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
public class PlayerWarnings {

    String player;

    List<Warning> warnings = new ArrayList<>();

    public static PlayerWarnings make(@NotNull String playerName) {
        PlayerWarnings entity = new PlayerWarnings();
        entity.player = playerName;
        return entity;
    }
}

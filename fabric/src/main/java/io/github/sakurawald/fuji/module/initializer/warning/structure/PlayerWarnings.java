package io.github.sakurawald.fuji.module.initializer.warning.structure;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class PlayerWarnings {

    public String player;

    public List<Warning> warnings = new ArrayList<>();

    public PlayerWarnings(String player) {
        this.player = player;
    }
}

package io.github.sakurawald.module.initializer.gameplay.carpet.fake_player_manager.config.model;

import java.util.ArrayList;
import java.util.List;

public class FakePlayerManagerConfigModel {
    public List<List<Integer>> caps_limit_rule = new ArrayList<>() {
        {
            this.add(List.of(1, 0, 2));
        }
    };

    public int renew_duration_ms = 1000 * 60 * 60 * 12;

    public String transform_name = "_fake_%name%";

}

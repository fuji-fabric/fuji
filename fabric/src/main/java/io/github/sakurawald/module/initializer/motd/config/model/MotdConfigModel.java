package io.github.sakurawald.module.initializer.motd.config.model;

import java.util.ArrayList;
import java.util.List;

public class MotdConfigModel {
    public List<String> list = new ArrayList<>() {
        {
            this.add("<gradient:#FF66B2:#FFB5CC>Pure Survival %server:version% / Up %server:uptime% ❤ Discord Group XXX</gradient><newline><gradient:#99CCFF:#BBDFFF>%fuji:server_playtime%🔥 %fuji:server_mined%⛏ %fuji:server_placed%🔳 %fuji:server_killed%🗡 %fuji:server_moved%🌍");
        }
    };
}

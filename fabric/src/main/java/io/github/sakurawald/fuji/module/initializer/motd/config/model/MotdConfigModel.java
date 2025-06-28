package io.github.sakurawald.fuji.module.initializer.motd.config.model;

import io.github.sakurawald.fuji.core.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.motd.structure.MotdEntry;

import java.util.ArrayList;
import java.util.List;

public class MotdConfigModel {

    @Document("""
        Defined `motd` entry.
        """)
    public List<MotdEntry> entries = new ArrayList<>() {
        {
            this.add(new MotdEntry("<gradient:#FF66B2:#FFB5CC>Pure Survival %server:version% / Up %server:uptime% ❤ Discord Group XXX</gradient><newline><gradient:#99CCFF:#BBDFFF>%fuji:server_playtime%🔥 %fuji:server_mined%⛏ %fuji:server_placed%🔳 %fuji:server_killed%🗡 %fuji:server_moved%", null));

            this.add(new MotdEntry("Please put your icon in `config/fuji/modules/motd/icon/` dir.", "icon-1.png"));
        }
    };
}

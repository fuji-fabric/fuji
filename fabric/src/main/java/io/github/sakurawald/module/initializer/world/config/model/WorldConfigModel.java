package io.github.sakurawald.module.initializer.world.config.model;

import java.util.ArrayList;
import java.util.List;

public class WorldConfigModel {

    public Blacklist blacklist = new Blacklist();

    public static class Blacklist {
        public List<String> dimension_list = new ArrayList<>() {
            {
                this.add("minecraft:overworld");
                this.add("minecraft:the_nether");
                this.add("minecraft:the_end");
            }
        };
    }
}

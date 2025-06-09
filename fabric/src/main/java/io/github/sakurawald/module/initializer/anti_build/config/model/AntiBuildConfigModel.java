package io.github.sakurawald.module.initializer.anti_build.config.model;

import java.util.HashSet;
import java.util.Set;

public class AntiBuildConfigModel {

    public Anti anti = new Anti();

    public static class Anti {

        public BreakBlock break_block = new BreakBlock();
        public PlaceBlock place_block = new PlaceBlock();
        public InteractItem interact_item = new InteractItem();
        public InteractBlock interact_block = new InteractBlock();
        public InteractEntity interact_entity = new InteractEntity();

        public static class BreakBlock {
            public Set<String> id = new HashSet<>() {
                {
                    this.add("minecraft:gold_block");
                }
            };
        }

        public static class PlaceBlock {
            public Set<String> id = new HashSet<>() {
                {
                    this.add("minecraft:tnt");
                }
            };
        }

        public static class InteractItem {
            public Set<String> id = new HashSet<>() {
                {
                    this.add("minecraft:lava_bucket");
                }
            };
        }

        public static class InteractBlock {
            public Set<String> id = new HashSet<>() {
                {
                    this.add("minecraft:lever");
                }
            };
        }

        public static class InteractEntity {
            public Set<String> id = new HashSet<>() {
                {
                    this.add("minecraft:villager");
                }
            };

        }
    }
}

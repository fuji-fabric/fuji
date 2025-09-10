package io.github.sakurawald.fuji.module.initializer.anti_build.config.model;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AntiBuildConfigModel {

    Anti anti = new Anti();

    @Data
    @NoArgsConstructor
    public static class Anti {

        BreakBlock breakBlock = new BreakBlock();
        PlaceBlock placeBlock = new PlaceBlock();
        InteractItem interactItem = new InteractItem();
        InteractBlock interactBlock = new InteractBlock();
        InteractEntity interactEntity = new InteractEntity();
        AttackEntity attackEntity = new AttackEntity();

        @Data
        @NoArgsConstructor
        public static class BreakBlock {
            Set<String> id = new HashSet<>() {
                {
                    this.add("minecraft:gold_block");
                }
            };
        }

        @Data
        @NoArgsConstructor
        public static class PlaceBlock {
            Set<String> id = new HashSet<>() {
                {
                    this.add("minecraft:tnt");
                }
            };
        }

        @Data
        @NoArgsConstructor
        public static class InteractItem {
            Set<String> id = new HashSet<>() {
                {
                    this.add("minecraft:lava_bucket");
                }
            };
        }

        @Data
        @NoArgsConstructor
        public static class InteractBlock {
            Set<String> id = new HashSet<>() {
                {
                    this.add("minecraft:lever");
                }
            };
        }

        @Data
        @NoArgsConstructor
        public static class InteractEntity {
            Set<String> id = new HashSet<>() {
                {
                    this.add("minecraft:villager");
                }
            };

        }

        @Data
        @NoArgsConstructor
        public static class AttackEntity {
            Set<String> id = new HashSet<>() {
                {
                    this.add("minecraft:villager");
                }
            };

        }
    }
}

package mod.fuji.module.initializer.anti_build.config.model;

import com.google.gson.annotations.SerializedName;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AntiBuildConfigModel {

    @SerializedName(value = "anti_action_types", alternate = {"anti", "anti_types"})
    AntiActionTypes antiActionTypes = new AntiActionTypes();

    @Data
    @NoArgsConstructor
    public static class AntiActionTypes {

        BreakBlock breakBlock = new BreakBlock();
        PlaceBlock placeBlock = new PlaceBlock();
        InteractItem interactItem = new InteractItem();
        InteractBlock interactBlock = new InteractBlock();
        InteractEntity interactEntity = new InteractEntity();
        AttackEntity attackEntity = new AttackEntity();

        @Data
        @NoArgsConstructor
        public static class BreakBlock {
            boolean enable = false;
            Set<String> id = new HashSet<>() {
                {
                    this.add("minecraft:gold_block");
                }
            };
        }

        @Data
        @NoArgsConstructor
        public static class PlaceBlock {
            boolean enable = false;
            Set<String> id = new HashSet<>() {
                {
                    this.add("minecraft:tnt");
                }
            };
        }

        @Data
        @NoArgsConstructor
        public static class InteractItem {
            boolean enable = false;
            Set<String> id = new HashSet<>() {
                {
                    this.add("minecraft:lava_bucket");
                }
            };
        }

        @Data
        @NoArgsConstructor
        public static class InteractBlock {
            boolean enable = false;
            Set<String> id = new HashSet<>() {
                {
                    this.add("minecraft:lever");
                }
            };
        }

        @Data
        @NoArgsConstructor
        public static class InteractEntity {
            boolean enable = false;
            Set<String> id = new HashSet<>() {
                {
                    this.add("minecraft:villager");
                }
            };

        }

        @Data
        @NoArgsConstructor
        public static class AttackEntity {
            boolean enable = false;
            Set<String> id = new HashSet<>() {
                {
                    this.add("minecraft:villager");
                }
            };

        }
    }
}

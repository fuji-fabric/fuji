package mod.fuji.module.initializer.cleaner.config.model;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.cleaner.structure.CleanerMatcher;
import mod.fuji.module.initializer.cleaner.structure.CleanupMethod;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CleanerConfigModel {

    @Document(id = 1751826888205L, value = """
        The `cron` expression used to trigger entity `cleaner`.
        """)
    String cron = "0 * * ? * * *";

    @Document(id = 1751826892450L, value = """
        Ignore and never remove entities that meet the condition.
        """)
    Ignore ignore = new Ignore();

    @Data
    @NoArgsConstructor
    public static class Ignore {

        @SerializedName(value = "ignore_item_entity", alternate = "ignoreItemEntity")
        boolean ignoreItemEntity = false;

        @SerializedName(value = "ignore_living_entity", alternate = "ignoreLivingEntity")
        boolean ignoreLivingEntity = true;

        @SerializedName(value = "ignore_named_entity", alternate = "ignoreNamedEntity")
        boolean ignoreNamedEntity = true;

        @SerializedName(value = "ignore_entity_with_vehicle", alternate = "ignoreEntityWithVehicle")
        boolean ignoreEntityWithVehicle = true;

        @SerializedName(value = "ignore_entity_with_passengers", alternate = "ignoreEntityWithPassengers")
        boolean ignoreEntityWithPassengers = true;

        @SerializedName(value = "ignore_glowing_entity", alternate = "ignoreGlowingEntity")
        boolean ignoreGlowingEntity = true;

        @SerializedName(value = "ignore_leashed_entity", alternate = "ignoreLeashedEntity")
        boolean ignoreLeashedEntity = true;

    }

    @Document(id = 1751826890149L, value = """
        The `translatable key` to `age` map.

        The `translatable key` defines which `entity` should we remove.
        The `living ticks` says that we `only` removes the entity whose `age` greater than defined value.

        The unit of `age` is `game tick` (20 ticks = 1 sec).
        """)
    List<CleanerMatcher> matchers = new ArrayList<>() {
        {
            this.add(new CleanerMatcher(false, "block.minecraft.sand", 1200, CleanupMethod.DISCARD));
            this.add(new CleanerMatcher(false, "item.minecraft.ender_pearl", 1200, CleanupMethod.DISCARD));
            this.add(new CleanerMatcher(false, "block.minecraft.white_carpet", 1200, CleanupMethod.DISCARD));
            this.add(new CleanerMatcher(false, "block.minecraft.cobblestone", 1200, CleanupMethod.DISCARD));
            this.add(new CleanerMatcher(false, "entity.minecraft.skeleton", 1200, CleanupMethod.KILL));
        }
    };

}

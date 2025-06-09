package io.github.sakurawald.module.initializer.cleaner.config.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class CleanerConfigModel {

    public String cron = "0 * * ? * * *";

    public Map<String, Integer> key2age = new HashMap<>() {
        {
            this.put("block.minecraft.sand", 1200);
            this.put("item.minecraft.ender_pearl", 1200);
            this.put("block.minecraft.white_carpet", 1200);
            this.put("block.minecraft.cobblestone", 1200);
        }
    };

    public Ignore ignore = new Ignore();

    public static class Ignore {
        @SerializedName(value = "ignore_item_entity", alternate = "ignoreItemEntity")
        public boolean ignore_item_entity = false;
        @SerializedName(value = "ignore_living_entity", alternate = "ignoreLivingEntity")
        public boolean ignore_living_entity = true;
        @SerializedName(value = "ignore_named_entity", alternate = "ignoreNamedEntity")
        public boolean ignore_named_entity = true;
        @SerializedName(value = "ignore_entity_with_vehicle", alternate = "ignoreEntityWithVehicle")
        public boolean ignore_entity_with_vehicle = true;
        @SerializedName(value = "ignore_entity_with_passengers", alternate = "ignoreEntityWithPassengers")
        public boolean ignore_entity_with_passengers = true;
        @SerializedName(value = "ignore_glowing_entity", alternate = "ignoreGlowingEntity")
        public boolean ignore_glowing_entity = true;
        @SerializedName(value = "ignore_leashed_entity", alternate = "ignoreLeashedEntity")
        public boolean ignore_leashed_entity = true;
    }

}

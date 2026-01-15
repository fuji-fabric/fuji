package mod.fuji.module.initializer.back.config.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import mod.fuji.core.document.annotation.Document;

@Data
@NoArgsConstructor
public class BackConfigModel {

    @Document(id = 1751825546269L, value = """
        Do not push the current `location` if it is too `close` to the most recently pushed `location`.
        """)
    @SerializedName(value = "do_not_push_back_location_if_closer_than_n_blocks", alternate = "ignore_distance")
    double doNotPushBackLocationIfCloserThanNBlocks = 32d;

    @SerializedName(value = "push_back_location_on_player_death", alternate = "enable_back_on_death")
    boolean pushBackLocationOnPlayerDeath = true;

    @SerializedName(value = "push_back_location_on_player_teleport", alternate = "enable_back_on_teleport")
    boolean pushBackLocationOnPlayerTeleport = true;

    @SerializedName(value = "max_number_of_back_locations_to_save", alternate = {"max_back_location_entries_to_save", "max_back_locations_to_save"})
    int maxNumberOfBackLocationsToSave = 3;

}

package io.github.sakurawald.module.initializer.command_toolbox.sit.config.model;

import com.google.gson.annotations.SerializedName;

public class SitConfigModel {

    @SerializedName(value = "allow_right_click_a_stair_block_or_slab_block_to_sit", alternate = "allow_right_click_sit")
    public boolean allow_right_click_a_stair_block_or_slab_block_to_sit = true;
    public boolean allow_sneaking_to_sit = false;
    public boolean require_empty_hand_to_sit = false;
    public boolean require_no_opaque_block_above_to_sit = true;
    public int max_distance_to_sit = -1;
}

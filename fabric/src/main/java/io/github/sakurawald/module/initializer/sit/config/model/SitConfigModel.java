package io.github.sakurawald.module.initializer.sit.config.model;

public class SitConfigModel {

    public RightClickToSit right_click_to_sit = new RightClickToSit();
    public static class RightClickToSit {
        public boolean enable = true;
        public boolean allow_sneaking_to_sit = false;
        public boolean require_empty_hand_to_sit = false;
        public boolean require_no_opaque_block_above_to_sit = true;
        public int max_distance_to_sit = -1;
    }

}

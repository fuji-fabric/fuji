package mod.fuji.module.initializer.sit.config.model;

import mod.fuji.core.document.annotation.Document;

public class SitConfigModel {

    public RightClickToSit right_click_to_sit = new RightClickToSit();
    public static class RightClickToSit {
        @Document(id = 1751826996214L, value = """
            Allow `right click` a `stair block` or `slab block` to sit?
            """)
        public boolean enable = true;
        public boolean allow_sneaking_to_sit = false;
        public boolean require_empty_hand_to_sit = false;
        public boolean require_no_opaque_block_above_to_sit = true;
        public int max_distance_to_sit = -1;
    }

}

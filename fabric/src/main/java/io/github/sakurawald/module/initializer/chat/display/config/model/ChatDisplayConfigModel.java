package io.github.sakurawald.module.initializer.chat.display.config.model;

public class ChatDisplayConfigModel {
    public int expiration_duration_s = 3600;

    public ReplacePattern replace_pattern = new ReplacePattern();
    public static class ReplacePattern {
        public String item_display = "\\[item\\]";
        public String inv_display = "\\[inv\\]";
        public String ender_display = "\\[ender\\]";
    }
}

package io.github.sakurawald.module.initializer.nametag.config.model;

public class NametagConfigModel {

    public String update_cron = "* * * ? * *";

    public Style style = new Style();
    public Render render = new Render();

    public static class Style {
        public String text = "<#B1B2FF>%fuji:player_playtime%\uD83D\uDD25 %fuji:player_mined%⛏ %fuji:player_placed%\uD83D\uDD33 %fuji:player_killed%\uD83D\uDDE1 %fuji:player_moved%\uD83C\uDF0D\n<dark_green>%player:displayname_visual%"; // escape Unicode

        public Offset offset = new Offset();
        public Size size = new Size();
        public Scale scale = new Scale();
        public Brightness brightness = new Brightness();
        public Shadow shadow = new Shadow();
        public Color color = new Color();

        public static class Offset {
            public float x = 0f;
            public float y = 0.2f;
            public float z = 0f;
        }

        public static class Size {
            public float height = 0f;
            public float width = 0f;
        }

        public static class Scale {
            public float x = 1.0f;
            public float y = 1.0f;
            public float z = 1.0f;
        }

        public static class Brightness {
            public boolean override_brightness = false;
            public int block = 15;
            public int sky = 15;
        }

        public static class Shadow {
            public boolean shadow = false;
            public float shadow_radius = 0f;
            public float shadow_strength = 1f;
        }

        public static class Color {
            public int background = 1073741824;
            public byte text_opacity = -1;
        }

    }

    public static class Render {
        public boolean see_through_blocks = false;
        public float view_range = 1.0f;
    }
}

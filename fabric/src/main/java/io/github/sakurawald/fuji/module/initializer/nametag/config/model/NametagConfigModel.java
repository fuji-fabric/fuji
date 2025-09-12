package io.github.sakurawald.fuji.module.initializer.nametag.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;

public class NametagConfigModel {

    @Document(id = 1751824986864L, value = """
        The `cron` expression used to `update` nametags.
        """)
    public String update_cron = "* * * ? * *";

    @Document(id = 1751824993802L, value = """
        Define the `style` of nametag.
        """)
    public Style style = new Style();
    public static class Style {
        public String text = "<#B1B2FF>%fuji:player_playtime%\uD83D\uDD25 %fuji:player_mined%⛏ %fuji:player_placed%\uD83D\uDD33 %fuji:player_killed%\uD83D\uDDE1 %fuji:player_moved%\uD83C\uDF0D\n<dark_green>%player:displayname_visual%"; // escape Unicode

        public Style.Offset offset = new Style.Offset();
        public Size size = new Size();
        public Scale scale = new Scale();
        public Brightness brightness = new Brightness();
        public Shadow shadow = new Shadow();
        public Color color = new Color();

        public static class Offset {
            public float x = 0f;
            public float y =
            #if MC_VER <= MC_1_20_1
                0.7f;
            #elif MC_VER > MC_1_20_1
                0.2f;
            #endif
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

    @Document(id = 1751825000155L, value = """
        Define the `render` logic of `nametags`.
        """)
    public Render render = new Render();
    public static class Render {
        public boolean see_through_blocks = false;
        public float view_range = 1.0f;
    }

    public Interpolator interpolator = new Interpolator();
    public static class Interpolator {
        public Duration duration = new Duration();
        public static class Duration {
            public int interpolate_duration = 1;
        }

    }

}

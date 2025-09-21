package mod.fuji.module.initializer.chat.style.model;

import mod.fuji.core.document.annotation.Document;

public class ChatStyleConfigModel {

    @Document(id = 1751826667553L, value = """
        Customize the `chat style`.
        """)
    public Style style = new Style();
    public static class Style {
        @Document(id = 1751826670256L, value = """
            The `format` used in `sender` component.
            """)
        public String sender = "<#B1B2FF>[%fuji:player_playtime%\uD83D\uDD25 %fuji:player_mined%⛏ %fuji:player_placed%\uD83D\uDD33 %fuji:player_killed%\uD83D\uDDE1 %fuji:player_moved%\uD83C\uDF0D]<reset> <<dark_green><click:suggest_command:'/msg %player:name% '><hover:show_text:'Time: %fuji:date%<newline><italic>Click to Message'>%player:displayname_visual%</hover></click></dark_green>> "; // use emoji

        @Document(id = 1751826672451L, value = """
            The `format` used in `content` component.
            """)
        public String content = "%s";
    }

}

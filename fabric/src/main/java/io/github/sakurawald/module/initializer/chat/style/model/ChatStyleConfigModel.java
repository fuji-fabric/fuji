package io.github.sakurawald.module.initializer.chat.style.model;

public class ChatStyleConfigModel {

    public Style style = new Style();

    public static class Style {
        public String sender = "<#B1B2FF>[%fuji:player_playtime%\uD83D\uDD25 %fuji:player_mined%⛏ %fuji:player_placed%\uD83D\uDD33 %fuji:player_killed%\uD83D\uDDE1 %fuji:player_moved%\uD83C\uDF0D]<reset> <<dark_green><click:suggest_command:'/msg %player:name% '><hover:show_text:'Time: %fuji:date%<newline><italic>Click to Message'>%player:displayname_visual%</hover></click></dark_green>> "; // use emoji

        public String content = "%s";
    }

}

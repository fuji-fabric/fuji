package io.github.sakurawald.fuji.module.initializer.tab.config.model;

import io.github.sakurawald.fuji.core.annotation.Document;

import java.util.ArrayList;
import java.util.List;

public class TabListConfigModel {
    @Document("""
        The `cron` expression used to `update` the tab list.
        """)
    public String update_cron = "* * * ? * *";

    @Document("""
        Define the style of tab list.
        """)
    public Style style = new Style();
    public static class Style {
        public List<String> header = new ArrayList<>() {
            {
                this.add("<#FFA1F5>PlayerList<newline>------%server:online%/%server:max_players%------");
            }
        };
        public List<String> body = new ArrayList<>() {
            {
                this.add("<rainbow>%player:displayname_visual%");
            }
        };
        public List<String> footer = new ArrayList<>() {
            {
                this.add("<#FFA1F5>-----------------<newline>TPS: %server:tps_colored% PING: %player:ping_colored%<newline><rainbow>Memory: %server:used_ram%/%server:max_ram% MB<newline>%fuji:rotate Welcome to the server. %");
            }

        };
    }
}

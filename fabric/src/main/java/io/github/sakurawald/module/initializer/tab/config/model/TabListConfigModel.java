package io.github.sakurawald.module.initializer.tab.config.model;

import java.util.ArrayList;
import java.util.List;

public class TabListConfigModel {

    public Style style = new Style();
    public String update_cron = "* * * ? * *";

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

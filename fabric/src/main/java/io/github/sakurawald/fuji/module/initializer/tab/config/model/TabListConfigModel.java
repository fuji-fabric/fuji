package io.github.sakurawald.fuji.module.initializer.tab.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;

import java.util.ArrayList;
import java.util.List;

public class TabListConfigModel {
    @Document(id = 1751826903818L, value = """
        The `cron` expression used to `update` the tab list.
        """)
    public String update_cron = "* * * ? * *";

    @Document(id = 1751826905489L, value = """
        Define the style of tab list.
        """)
    public Style style = new Style();
    public static class Style {
        public List<String> header = new ArrayList<>() {
            {
                this.add("<rainbow><strikethrough>                              </rainbow><newline><#FFA1F5><b>Server Name</#FFA1F5><newline><grey><b>Online players: %server:online%</grey>");
            }
        };
        public List<String> body = new ArrayList<>() {
            {
                this.add("<gradient:#FFA1F5:#BFBDFB:#6ECBFF>%player:displayname_visual%");
            }
        };
        public List<String> footer = new ArrayList<>() {
            {
                this.add("<grey><b>TPS: %server:tps_colored% MSPT: %server:mspt_colored% PING: %player:ping_colored%</grey><newline><grey><b>Memory: %server:used_ram%/%server:max_ram% MB</grey><newline><#FFA1F5><b>%fuji:rotate Welcome to the server. %</#FFA1F5><newline><rainbow><strikethrough>                              </rainbow>");
            }

        };
    }
}

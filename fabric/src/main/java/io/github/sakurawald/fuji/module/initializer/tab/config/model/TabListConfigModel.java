package io.github.sakurawald.fuji.module.initializer.tab.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TabListConfigModel {
    @Document(id = 1751826903818L, value = """
        The `cron` expression used to `update` the tab list.
        """)
    String updateCron = "* * * ? * *";

    @Document(id = 1751826905489L, value = """
        Define the style of tab list.
        """)
    Style style = new Style();
    @Data
    @NoArgsConstructor
    public static class Style {
        boolean enableHeader = true;
        boolean enableFooter = true;

        List<String> header = new ArrayList<>() {
            {
                this.add("<rainbow><strikethrough>                              </strikethrough></rainbow><newline><#FFA1F5><b>Server Name</b><newline><grey><b>Online players: %server:online%</b></grey>");
            }
        };
        List<String> body = new ArrayList<>() {
            {
                this.add("<gradient:#FFA1F5:#BFBDFB:#6ECBFF>%player:displayname_visual%");
            }
        };
        List<String> footer = new ArrayList<>() {
            {
                this.add("<grey><b>TPS: %server:tps_colored% MSPT: %server:mspt_colored% PING: %player:ping_colored%</b></grey><newline><grey><b>Memory: %server:used_ram%/%server:max_ram% MB</b></grey><newline><#FFA1F5><b>%fuji:rotate Welcome to the server. %<newline><rainbow><strikethrough>                              </strikethrough></rainbow>");
            }

        };
    }
}

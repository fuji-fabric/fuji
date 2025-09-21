package mod.fuji.module.initializer.motd.config.model;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.motd.structure.MotdEntry;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MotdConfigModel {

    @Document(id = 1751826857082L, value = """
        Defined `motd` entry.
        """)
    @SerializedName(value = "messages", alternate = "entries")
    List<MotdEntry> messages = new ArrayList<>() {
        {
            this.add(new MotdEntry("<gradient:#FFA1F5:#BFBDFB:#6ECBFF>Pure Survival %server:version% / Up %server:uptime% ❤ Discord Group XXX</gradient><newline><gradient:#99CCFF:#BBDFFF>%fuji:server_playtime%🔥 %fuji:server_mined%⛏ %fuji:server_placed%🔳 %fuji:server_killed%🗡 %fuji:server_moved%\uD83C\uDF0D", null));

            this.add(new MotdEntry("Please put your icon in `config/fuji/modules/motd/icon/` dir.", "icon-1.png"));
        }
    };

    @Document(id = 1753454689220L, value = """
        This section is used to customize the `players info` in the server metadata.
        """)
    PlayersInfo playersInfo = new PlayersInfo();

    @Data
    @NoArgsConstructor
    public static class PlayersInfo {
        @Document(id = 1753456677368L, value = """
            Increase `the number of max players` by a `random delta number`.
            """)
        MaxPlayers maxPlayers = new MaxPlayers();
        @Data
        @NoArgsConstructor
        public static class MaxPlayers {
            int deltaMin = 0;
            int deltaMax = 0;
        }

        @Document(id = 1753456694624L, value = """
            Increase `the number of online players` by a `random delta number`.
            """)
        OnlinePlayers onlinePlayers = new OnlinePlayers();
        @Data
        @NoArgsConstructor
        public static class OnlinePlayers {
            int deltaMin = 0;
            int deltaMax = 0;
        }

        @Document(id = 1753456710740L, value = """
            Customize the `hover text` when you `hover` on the `players info` area.
            """)
        HoverText hoverText = new HoverText();
        @Data
        @NoArgsConstructor
        public static class HoverText {
            boolean enable = true;

            List<String> lines = new ArrayList<>() {
                {
                    this.add("§aWelcome to the server!");
                    this.add("§aServer version: %server:version%");
                    this.add("§b§o§lJoin to play now.");
                }
            };
        }

    }

    @Document(id = 1753457245222L, value = """
            Customize the `version text`.

            <red>NOTE: Once you enable this feature, the `ping result` will not be displayed on the client.
            """)
    VersionText versionText = new VersionText();
    @Data
    @NoArgsConstructor
    public static class VersionText {
        boolean enable = false;
        String text = "§bJoin to play now.";
    }
}

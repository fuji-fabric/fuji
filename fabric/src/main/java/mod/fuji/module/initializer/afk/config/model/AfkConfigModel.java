package mod.fuji.module.initializer.afk.config.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import mod.fuji.core.document.annotation.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class AfkConfigModel {

    @Document(id = 1751825634067L, value = """
        Define the `display name` of a player in afk state.
        """)
    @SerializedName(value = "afk_display_name_format", alternate = "format")
    String afkDisplayNameFormat = "<gray>[AFK] %player:displayname_visual%";

    @Document(id = 1751825643038L, value = """
        An afk checker is used to compare and mark a player as in afk state.
        """)
    AfkChecker afkChecker = new AfkChecker();
    @Data
    @NoArgsConstructor
    public static class AfkChecker {
        public String cron = "0 0/5 * ? * *";
    }

    @Document(id = 1751825654901L, value = """
        Define commands to be executed when enter or leave the afk state.
        """)
    AfkEvent afkEvent = new AfkEvent();
    @Data
    @NoArgsConstructor
    public static class AfkEvent {

        List<String> onEnterAfk = new ArrayList<>() {
            {
                this.add("send-broadcast <gold>Player %player:name% is now afk");

            }
        };

        List<String> onLeaveAfk = new ArrayList<>() {
            {
                this.add("send-broadcast <gold>Player %player:name% is no longer afk");
            }
        };
    }

}

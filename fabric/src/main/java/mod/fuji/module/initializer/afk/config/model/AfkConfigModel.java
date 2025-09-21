package mod.fuji.module.initializer.afk.config.model;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.document.annotation.Document;

import java.util.ArrayList;
import java.util.List;

public class AfkConfigModel {
    @Document(id = 1751825634067L, value = """
        The `display name` of an afk player in `tab` list.
        """)
    @SerializedName(value = "afk_display_name_format", alternate = "format")
    public String afk_display_name_format = "<gray>[AFK] %player:displayname_visual%";

    @Document(id = 1751825643038L, value = """
        Afk checker is `triggered` periodically.
        To check whether a player has any input action.
        And mark the player as `afk` if there is no `action` for too long.
        """)
    public AfkChecker afk_checker = new AfkChecker();
    public static class AfkChecker {
        @Document(id = 1751825650915L, value = """
            The `cron` expression used to trigger `afk checker`.
            """)
        public String cron = "0 0/5 * ? * *";
    }

    @Document(id = 1751825654901L, value = """
        Define commands to run on afk events.
        """)
    public AfkEvent afk_event = new AfkEvent();
    public static class AfkEvent {
        @Document(id = 1751825659946L, value = "When a player enters afk state.")
        public List<String> on_enter_afk = new ArrayList<>() {
            {
                this.add("send-broadcast <gold>Player %player:name% is now afk");

            }
        };

        @Document(id = 1751826157602L, value = """
            When a player leaves afk state.
            """)
        public List<String> on_leave_afk = new ArrayList<>() {
            {
                this.add("send-broadcast <gold>Player %player:name% is no longer afk");
            }
        };
    }

}

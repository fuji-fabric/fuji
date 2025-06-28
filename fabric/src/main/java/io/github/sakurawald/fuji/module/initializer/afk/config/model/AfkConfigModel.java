package io.github.sakurawald.fuji.module.initializer.afk.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;

import java.util.ArrayList;
import java.util.List;

public class AfkConfigModel {
    @Document("""
        The display name of an afk player in `tab` list.
        """)
    public String format = "<gray>[AFK] %player:displayname_visual%";

    @Document("""
        Afk checker is `triggered` periodically.
        To check whether a player has any input action.
        And mark the player as `afk` if there is no `action` for too long.
        """)
    public AfkChecker afk_checker = new AfkChecker();
    public static class AfkChecker {
        @Document("""
        The `cron` expression used to trigger `afk checker`.
        """)
        public String cron = "0 0/5 * ? * *";
    }

    @Document("""
        Define commands to run on afk events.
        """)
    public AfkEvent afk_event = new AfkEvent();
    public static class AfkEvent {
        @Document("When a player enters afk state.")
        public List<String> on_enter_afk = new ArrayList<>() {
            {
                this.add("send-broadcast <gold>Player %player:name% is now afk");

            }
        };

        @Document("""
            When a player leaves afk state.
            """)
        public List<String> on_leave_afk = new ArrayList<>() {
            {
                this.add("send-broadcast <gold>Player %player:name% is no longer afk");
                this.add("effect give %player:name% minecraft:absorption 5 4");
            }
        };
    }

}

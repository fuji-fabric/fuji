package io.github.sakurawald.module.initializer.afk.config.model;

import java.util.ArrayList;
import java.util.List;

public class AfkConfigModel {
    public String format = "<gray>[AFK] %player:displayname_visual%";

    public AfkChecker afk_checker = new AfkChecker();
    public AfkEvent afk_event = new AfkEvent();

    public static class AfkChecker {
        public String cron = "0 0/5 * ? * *";
    }

    public static class AfkEvent {
        public List<String> on_enter_afk = new ArrayList<>() {
            {
                this.add("send-broadcast <gold>Player %player:name% is now afk");

            }
        };

        public List<String> on_leave_afk = new ArrayList<>() {
            {
                this.add("send-broadcast <gold>Player %player:name% is no longer afk");
                this.add("effect give %player:name% minecraft:absorption 5 4");
            }
        };
    }

}

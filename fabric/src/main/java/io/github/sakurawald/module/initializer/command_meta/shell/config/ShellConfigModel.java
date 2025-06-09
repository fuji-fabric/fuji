package io.github.sakurawald.module.initializer.command_meta.shell.config;

import java.util.ArrayList;
import java.util.List;

public class ShellConfigModel {

    public String enable_warning = "ENABLE THIS MODULE IS POTENTIAL TO HARM YOUR COMPUTER! YOU NEED TO CHANGE THIS FIELD INTO `CONFIRM` TO ENABLE THIS MODULE";
    public Security security = new Security();

    public static class Security {
        public boolean only_allow_console = true;
        public List<String> allowed_player_names = new ArrayList<>();
    }
}

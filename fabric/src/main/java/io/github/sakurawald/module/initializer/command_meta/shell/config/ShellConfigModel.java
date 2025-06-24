package io.github.sakurawald.module.initializer.command_meta.shell.config;

import io.github.sakurawald.core.annotation.Document;

import java.util.ArrayList;
import java.util.List;

public class ShellConfigModel {

    public String enable_warning = "ENABLE THIS MODULE IS POTENTIAL TO HARM YOUR COMPUTER! YOU NEED TO CHANGE THIS FIELD INTO `CONFIRM` TO ENABLE THIS MODULE";

    @Document("""
        Security options.
        """)
    public Security security = new Security();
    public static class Security {
        @Document("""
            Only the `console` can use the `/shell` command?
            """)
        public boolean only_allow_console = true;

        @Document("""
            Only these `players` can use the `/shell` command?
            """)
        public List<String> allowed_player_names = new ArrayList<>();
    }
}

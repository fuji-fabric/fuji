package mod.fuji.module.initializer.command_meta.shell.config;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.document.annotation.Document;

import java.util.ArrayList;
import java.util.List;

public class ShellConfigModel {

    @SerializedName(value = "DANGER", alternate = "enable_warning")
    public String DANGER = "ENABLE THIS MODULE IS POTENTIAL TO HARM YOUR COMPUTER! YOU NEED TO CHANGE THIS FIELD INTO `CONFIRM` TO ENABLE THIS MODULE";

    @Document(id = 1751824744477L, value = """
        Security options.
        """)
    public Security security = new Security();
    public static class Security {
        @Document(id = 1751824750326L, value = """
            Only the `console` can use the `/shell` command?
            """)
        public boolean only_allow_console = true;

        @Document(id = 1751824756091L, value = """
            Only these `players` can use the `/shell` command?
            """)
        public List<String> allowed_player_names = new ArrayList<>();
    }
}

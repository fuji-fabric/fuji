package io.github.sakurawald.fuji.module.initializer.command_spy.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.document.annotation.Document;

import java.util.ArrayList;
import java.util.List;

public class CommandSpyConfigModel {

    public boolean spy_on_console = false;

    @Document(id = 1751826793929L, value = """
        Ignore and never spy on these commands.
        """)
    @SerializedName(value = "ignore_commands", alternate = "ignore")
    public List<String> ignore_commands = new ArrayList<>() {
        {
            this.add("login.*");
        }
    };

    @Document(id = 1751826795935L, value = """
        The `only spy on these commands mode`.
        """)
    public OnlySpyTheseCommands only_spy_these_commands = new OnlySpyTheseCommands();
    public static class OnlySpyTheseCommands {

        @Document(id = 1751826797690L, value = """
            Should we `only` spy on these commands?
            """)
        public boolean enable = false;

        public List<String> commands = new ArrayList<>() {
            {
                this.add("tpa .+");
                this.add("back .+");
                this.add("home .+");
            }
        };
    }
}

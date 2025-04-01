package io.github.sakurawald.module.initializer.command_spy.config.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CommandSpyConfigModel {

    @SerializedName("ignore")
    public List<String> ignore_commands = new ArrayList<>() {
        {
            this.add("login.*");
        }
    };

    public OnlySpyTheseCommands only_spy_these_commands;
    public static class OnlySpyTheseCommands {
        public boolean enable = false;
        public List<String> commands = new ArrayList<>() {
            {
                this.add("tpa .+");
                this.add("back .+");
                this.add("home .+");
            }
        };
    }

    public boolean spy_on_console = false;
}

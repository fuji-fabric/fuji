package mod.fuji.module.initializer.command_event.config.model;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.document.annotation.Document;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommandEventConfigModel {

    @Document(id = 1751826631166L, value = """
        Define `commands` to be execute on `specific events`.
        """)
    Event event = new Event();

    @Data
    @NoArgsConstructor
    public static class Event {
        OnPlayerDeath onPlayerDeath = new OnPlayerDeath();
        AfterPlayerBreakBlock afterPlayerBreakBlock = new AfterPlayerBreakBlock();
        AfterPlayerPlaceBlock afterPlayerPlaceBlock = new AfterPlayerPlaceBlock();
        AfterPlayerRespawn afterPlayerRespawn = new AfterPlayerRespawn();
        AfterPlayerChangeWorld afterPlayerChangeWorld = new AfterPlayerChangeWorld();
        OnPlayerFirstJoined onPlayerFirstJoined = new OnPlayerFirstJoined();
        OnPlayerJoined onPlayerJoined = new OnPlayerJoined();
        OnPlayerLeft onPlayerLeft = new OnPlayerLeft();

        @Data
        @NoArgsConstructor
        public static class OnPlayerDeath {
            boolean enable = true;
            @SerializedName(value = "commands", alternate = "command_list")
            List<String> commands = new ArrayList<>() {
                {
                    this.add("send-actionbar %player:name% <pink>You just died.");
                }
            };
        }

        @Data
        @NoArgsConstructor
        public static class AfterPlayerBreakBlock {
            boolean enable = true;
            @SerializedName(value = "commands", alternate = "command_list")
            List<String> commands = new ArrayList<>() {
                {
                    this.add("send-actionbar %player:name% <pink>You just broke a block.");
                }
            };
        }

        @Data
        @NoArgsConstructor
        public static class AfterPlayerPlaceBlock {
            boolean enable = true;
            @SerializedName(value = "commands", alternate = "command_list")
            List<String> commands = new ArrayList<>() {
                {
                    this.add("send-actionbar %player:name% <pink>You just placed a block.");
                }
            };
        }

        @Data
        @NoArgsConstructor
        public static class AfterPlayerRespawn {
            boolean enable = true;

            @SerializedName(value = "commands", alternate = "command_list")
            List<String> commands = new ArrayList<>() {
                {
                    this.add("give %player:name% minecraft:apple 1");
                }
            };
        }

        @Data
        @NoArgsConstructor
        public static class AfterPlayerChangeWorld {
            boolean enable = true;

            @SerializedName(value = "commands", alternate = "command_list")
            List<String> commands = new ArrayList<>() {
                {
                    this.add("send-actionbar %player:name% <pink>You are in %world:id% dimension now!");
                }
            };
        }

        @Data
        @NoArgsConstructor
        public static class OnPlayerFirstJoined {
            boolean enable = true;

            @SerializedName(value = "commands", alternate = "command_list")
            List<String> commands = new ArrayList<>() {
                {
                    this.add("send-broadcast <yellow>Welcome new player %player:name% to join us!");
                }
            };
        }

        @Data
        @NoArgsConstructor
        public static class OnPlayerJoined {
            boolean enable = true;

            @SerializedName(value = "commands", alternate = "command_list")
            List<String> commands = new ArrayList<>() {
                {
                    this.add("send-title %player:name% --mainTitle \"<yellow>Welcome to the server.\"");
                }
            };
        }

        @Data
        @NoArgsConstructor
        public static class OnPlayerLeft {
            boolean enable = true;

            @SerializedName(value = "commands", alternate = "command_list")
            List<String> commands = new ArrayList<>() {
                {
                    this.add("send-broadcast <dark_grey>%player:name% left the server.");
                }
            };
        }
    }
}

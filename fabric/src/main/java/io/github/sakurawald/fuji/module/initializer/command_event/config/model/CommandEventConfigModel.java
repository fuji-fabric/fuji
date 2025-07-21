package io.github.sakurawald.fuji.module.initializer.command_event.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;

import java.util.ArrayList;
import java.util.List;

public class CommandEventConfigModel {

    @Document(id = 1751826631166L, value = """
        Define `commands` to be execute on `specific events`.
        """)
    public Event event = new Event();
    public static class Event {
        public OnPlayerDeath on_player_death = new OnPlayerDeath();
        public AfterPlayerBreakBlock after_player_break_block = new AfterPlayerBreakBlock();
        public AfterPlayerPlaceBlock after_player_place_block = new AfterPlayerPlaceBlock();
        public AfterPlayerRespawn after_player_respawn = new AfterPlayerRespawn();
        public AfterPlayerChangeWorld after_player_change_world = new AfterPlayerChangeWorld();
        public OnPlayerFirstJoined on_player_first_joined = new OnPlayerFirstJoined();
        public OnPlayerJoined on_player_joined = new OnPlayerJoined();
        public OnPlayerLeft on_player_left = new OnPlayerLeft();

        public static class OnPlayerDeath {
            public boolean enable = true;
            public List<String> command_list = new ArrayList<>() {
                {
                    this.add("send-actionbar %player:name% <pink>You just died.");
                }
            };
        }

        public static class AfterPlayerBreakBlock {
            public boolean enable = true;
            public List<String> command_list = new ArrayList<>() {
                {
                    this.add("send-actionbar %player:name% <pink>You just broke a block.");
                }
            };
        }

        public static class AfterPlayerPlaceBlock {
            public boolean enable = true;
            public List<String> command_list = new ArrayList<>() {
                {
                    this.add("send-actionbar %player:name% <pink>You just placed a block.");
                }
            };
        }

        public static class AfterPlayerRespawn {
            public boolean enable = true;
            public List<String> command_list = new ArrayList<>() {
                {
                    this.add("give %player:name% minecraft:apple 1");
                }
            };
        }

        public static class AfterPlayerChangeWorld {
            public boolean enable = true;
            public List<String> command_list = new ArrayList<>() {
                {
                    this.add("send-actionbar %player:name% <pink>You are in %world:id% dimension now!");
                }
            };
        }

        public static class OnPlayerFirstJoined {
            public boolean enable = true;
            public List<String> command_list = new ArrayList<>() {
                {
                    this.add("send-broadcast <yellow>Welcome new player %player:name% to join us!");
                }
            };
        }

        public static class OnPlayerJoined {
            public boolean enable = true;
            public List<String> command_list = new ArrayList<>() {
                {
                    this.add("send-title %player:name% --mainTitle \"<yellow>Welcome to the server.\"");
                }
            };
        }

        public static class OnPlayerLeft {
            public boolean enable = true;
            public List<String> command_list = new ArrayList<>() {
                {
                    this.add("send-broadcast <dark_grey>%player:name% left the server.");
                }
            };
        }
    }
}

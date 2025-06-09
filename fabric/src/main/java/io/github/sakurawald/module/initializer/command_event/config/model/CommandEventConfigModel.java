package io.github.sakurawald.module.initializer.command_event.config.model;

import java.util.ArrayList;
import java.util.List;

public class CommandEventConfigModel {

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
            public List<String> command_list = new ArrayList<>() {
                {
                    this.add("send-message %player:name% you just die.");
                }
            };
        }

        public static class AfterPlayerBreakBlock {
            public List<String> command_list = new ArrayList<>() {
                {
                    this.add("send-message %player:name% you just break a block.");
                    this.add("say dry run -> experience add %player:name% %fuji:random 2 8%");
                }
            };
        }

        public static class AfterPlayerPlaceBlock {
            public List<String> command_list = new ArrayList<>() {
                {
                    this.add("send-message %player:name% you just place a block.");
                }
            };
        }

        public static class AfterPlayerRespawn {
            public List<String> command_list = new ArrayList<>() {
                {
                    this.add("give %player:name% minecraft:apple 8");
                }
            };
        }

        public static class AfterPlayerChangeWorld {
            public List<String> command_list = new ArrayList<>() {
                {
                    this.add("send-message %player:name% You are in %world:id% now!");
                }
            };
        }

        public static class OnPlayerFirstJoined {
            public List<String> command_list = new ArrayList<>() {
                {
                    this.add("send-broadcast <rainbow>welcome new player %player:name% to join us!");
                }
            };
        }

        public static class OnPlayerJoined {
            public List<String> command_list = new ArrayList<>() {
                {
                    this.add("send-message %player:name% welcome to the server.");
                }
            };
        }

        public static class OnPlayerLeft {
            public List<String> command_list = new ArrayList<>() {
                {
                    this.add("send-broadcast %player:name% left the server.");
                }
            };
        }
    }
}

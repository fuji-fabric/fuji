package io.github.sakurawald.module.initializer.command_menu.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CommandMenuConfigModel {

    public OnShiftAndSwapHandsEvent onShiftAndSwapHandsEvent = new OnShiftAndSwapHandsEvent();
    public static class OnShiftAndSwapHandsEvent {
        public boolean enable = true;
        public List<String> commands = new ArrayList<>() {
            {
                this.add("command-menu open %player:name% example-menu");
            }
        };
    }

}

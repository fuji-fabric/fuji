package io.github.sakurawald.fuji.module.initializer.command_menu.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommandMenuConfigModel {

    @SerializedName(value = "onSneakingAndSwapHandsEvent", alternate = "onShiftAndSwapHandsEvent")
    public OnShiftAndSwapHandsEvent onSneakingAndSwapHandsEvent = new OnShiftAndSwapHandsEvent();
    public static class OnShiftAndSwapHandsEvent {
        public boolean enable = true;
        public List<String> commands = new ArrayList<>() {
            {
                this.add("command-menu open %player:name% example-menu");
            }
        };
    }

}

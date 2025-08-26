package io.github.sakurawald.fuji.module.initializer.command_toolbox.warp.structure;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class WarpDescriptor {

    public GlobalPos position;

    @SerializedName(value = "display_name", alternate = "name")
    public String displayName = "<blue>Display Name";
    public String item = "minecraft:painting";
    public List<String> lore = new ArrayList<>();

    public Event event = new Event();
    public static class Event {
        public OnWarped on_warped = new OnWarped();
        public static class OnWarped {
            public List<String> command_list = new ArrayList<>();
        }
    }

    public WarpDescriptor(@NotNull GlobalPos position) {
        this.position = position;
    }
}

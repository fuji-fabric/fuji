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

    GlobalPos position;

    @SerializedName(value = "display_name", alternate = "name")
    String displayName = "<blue>Display Name";
    String item = "minecraft:painting";
    List<String> lore = new ArrayList<>();

    transient String key;

    Event event = new Event();

    @Data
    @NoArgsConstructor
    public static class Event {
        OnWarped onWarped = new OnWarped();

        @Data
        @NoArgsConstructor
        public static class OnWarped {
            List<String> commandList = new ArrayList<>();
        }
    }

    public WarpDescriptor(@NotNull GlobalPos position) {
        this.position = position;
    }
}

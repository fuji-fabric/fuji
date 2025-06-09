package io.github.sakurawald.module.initializer.command_toolbox.warp.structure;

import io.github.sakurawald.core.structure.SpatialPose;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class WarpNode {

    public SpatialPose position;

    public String name = "<blue>Display Name";
    public String item = "minecraft:painting";
    public List<String> lore = new ArrayList<>();

    public Event event = new Event();
    public static class Event {
        public OnWarped on_warped = new OnWarped();
        public static class OnWarped {
            public List<String> command_list = new ArrayList<>();
        }
    }

    public WarpNode(SpatialPose position) {
        this.position = position;
    }
}

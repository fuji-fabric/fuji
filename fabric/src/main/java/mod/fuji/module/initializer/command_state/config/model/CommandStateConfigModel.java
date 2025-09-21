package mod.fuji.module.initializer.command_state.config.model;

import mod.fuji.module.initializer.command_state.structure.StateDescriptor;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommandStateConfigModel {

    List<StateDescriptor> stateDescriptors = new ArrayList<>() {
        {
            StateDescriptor isInOverworldState = new StateDescriptor(
                true,
                "is-in-overworld",
                new StateDescriptor.Definition(List.of("is-in-world? %player:name% minecraft:overworld")),
                3,
                new StateDescriptor.Events(List.of("send-message %player:name% <green>You entered the overworld dimension."), List.of("send-message %player:name% <green>You left the overworld dimension."))
            );
            this.add(isInOverworldState);


            StateDescriptor hasIronAndGoldState = new StateDescriptor(
                true,
                "has-iron-and-gold",
                new StateDescriptor.Definition(List.of(
                    "has-item? %player:name% minecraft:iron_ingot 16",
                    "has-item? %player:name% minecraft:gold_ingot 8"
                )),
                3,
                new StateDescriptor.Events(List.of("send-message %player:name% <green>You have `iron_ingot x 16` and `gold_ingot x 8`."), List.of("send-message %player:name% <green>You don't have `iron_ingot x 16` and `gold_ingot x 8`."))
            );
            this.add(hasIronAndGoldState);


            StateDescriptor canUseFlyCommandState = new StateDescriptor(
                true,
                "can-use-fly-command",
                new StateDescriptor.Definition(List.of(
                    "has-perm? %player:name% fuji.permission.fly"
                )),
                3,
                new StateDescriptor.Events(List.of("send-message %player:name% <green>You have gained access to `/fly` command."), List.of(
                    "send-message %player:name% <red>You temporary `/fly` command access has expired.",
                    "run as fake-op %player:name% fly false"
                ))
            );
            this.add(canUseFlyCommandState);


        }
    };

}

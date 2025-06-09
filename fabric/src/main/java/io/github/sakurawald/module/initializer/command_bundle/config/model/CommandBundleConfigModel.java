package io.github.sakurawald.module.initializer.command_bundle.config.model;

import io.github.sakurawald.core.command.structure.CommandRequirementDescriptor;
import io.github.sakurawald.module.initializer.command_bundle.structure.BundleCommandNode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CommandBundleConfigModel {

    public List<BundleCommandNode> entries = new ArrayList<>() {
        {
            /* level 4 commands */
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "my-command test-the-command-with-optional-arg <int int-arg-name> [str str-arg-name this is the default value]", List.of("say hello %player:name%", "say int is $int-arg-name", "say str is $str-arg-name")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "my-command test-the-command-with-literal-arg first-literal second-literal <str str-arg-name>", List.of("say hello %player:name%", "say str is $str-arg-name")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "my-command test-the-command-with-optional-arg-and-literal-arg <int int-arg-name> first-literal [str str-arg-name the default value can contains placeholder %player:name% in %world:name%]", List.of("say hello %player:name%", "say int is $int-arg-name", "say str is $str-arg-name")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "my-command test-the-command-with-a-greedy-string <int int-arg-name> first-literal [greedy-string greedy-string-arg-name this is the default value]", List.of("say hello %player:name%", "say int is $int-arg-name", "say str is $greedy-string-arg-name")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "give-apple-to-random-player", List.of("give %fuji:random_player% minecraft:apple %fuji:random 16 32%")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "shoot <entity-type entity-type-arg-name>", List.of("execute as %player:name% run summon $entity-type-arg-name ~ ~1 ~ {ExplosionPower:4,Motion:[3.0,0.0,0.0]}")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "strike", List.of("execute as %player:name% at @s run summon lightning_bolt ^ ^ ^10")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "gm <gamemode gamemode-arg>", List.of("run as player %player:name% gamemode $gamemode-arg")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "unbreakable", List.of("run as player %player:name% enchant %player:name% minecraft:unbreaking")));

            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "move-speed set <double double-arg>", List.of("run as player %player:name% attribute %player:name% minecraft:generic.movement_speed base set $double-arg")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "move-speed reset", List.of("run as player %player:name% attribute %player:name% minecraft:generic.movement_speed base set 0.10000000149011612")));

            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "warn <player player-arg> <greedy greedy-arg>", List.of("run as player %player:name% send-message $player-arg <red>You are warned: $greedy-arg")));

            /* level 0 commands */
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(0, null), "introduce-me", List.of("run as fake-op %player:name% me i am %player:name%")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(0, null), "rules", List.of("send-message %player:name% <rb>This is the rules of the server: <newline>blah blah...")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(0, null), "block-info <blockpos blockpos-arg-name>", List.of("run as fake-op %player:name% data get block $blockpos-arg-name")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(0, null), "entity-info <entity entity-arg-name>", List.of("run as fake-op %player:name% data get entity $entity-arg-name")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(0, null), "dice", List.of("say %player:name% just roll out %fuji:random 1 6% points.")));
        }
    };

}

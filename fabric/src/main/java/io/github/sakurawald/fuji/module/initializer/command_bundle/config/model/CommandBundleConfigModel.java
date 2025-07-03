package io.github.sakurawald.fuji.module.initializer.command_bundle.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.command.structure.CommandRequirementDescriptor;
import io.github.sakurawald.fuji.module.initializer.command_bundle.structure.BundleCommandNode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CommandBundleConfigModel {

    @Document("""
        Defined `bundle commands`.
        """)
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

            /* Game mode change. */
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "gm <gamemode gamemode-arg>", List.of("run as player %player:name% gamemode $gamemode-arg")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "gmc", List.of("run as player %player:name% gamemode creative")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "gms", List.of("run as player %player:name% gamemode survival")));

            /* Weather change. */
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "sun", List.of("weather clear")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "rain", List.of("weather rain")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "thunder", List.of("weather thunder")));

            /* Time change. */
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "day", List.of("time set day")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "night", List.of("time set night")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "midnight", List.of("time set midnight")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "moon", List.of("time set moon")));

            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "unbreakable", List.of("run as player %player:name% enchant %player:name% minecraft:unbreaking")));

            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "move-speed set <double double-arg>", List.of("run as player %player:name% attribute %player:name% minecraft:generic.movement_speed base set $double-arg")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(4, null), "move-speed reset", List.of("run as player %player:name% attribute %player:name% minecraft:generic.movement_speed base set 0.10000000149011612")));


            /* level 0 commands */
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(0, null), "introduce-me", List.of("run as fake-op %player:name% me i am %player:name%")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(0, null), "block-info <blockpos blockpos-arg-name>", List.of("run as fake-op %player:name% data get block $blockpos-arg-name")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(0, null), "entity-info <entity entity-arg-name>", List.of("run as fake-op %player:name% data get entity $entity-arg-name")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(0, null), "dice", List.of("say %player:name% just roll out %fuji:random 1 6% points.")));

            this.add(new BundleCommandNode(new CommandRequirementDescriptor(0, null), "plugins", List.of("send-message %player:name% Server Plugins (0): ")));
            this.add(new BundleCommandNode(new CommandRequirementDescriptor(0, null), "icanhasbukkit",
                List.of("send-message %player:name% <i>Checking version, please wait..."
                , "delay 2 send-message %player:name% This server is running Bukkit version (MC: %server:version%)"
                , "delay 3 send-message %player:name% <green>You are running the latest version")));
        }
    };

}

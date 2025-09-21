package mod.fuji.module.initializer.command_bundle.config.model;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.command.structure.CommandRequirementDescriptor;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.command_bundle.structure.BundleCommandNode;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommandBundleConfigModel {

    @Document(id = 1751826342919L, value = """
        Defined `bundle commands`.
        """)
    @SerializedName(value = "bundle_commands", alternate = "entries")
    List<BundleCommandNode> bundleCommands = new ArrayList<>() {
        {
            /* Register `/my-command` commands. */
            this.add(new BundleCommandNode("This command is used to test the `optional argument`.", new CommandRequirementDescriptor(4, null), "my-command a-command-with-optional-arg <int int-arg-name> [str str-arg-name this is the default value]", List.of("say hello %player:name%", "say int is $int-arg-name", "say str is $str-arg-name")));
            this.add(new BundleCommandNode("This command is used to test the `literal argument` and `required argument`.", new CommandRequirementDescriptor(4, null), "my-command a-command-with-required-arg first-literal second-literal <str str-arg-name>", List.of("say hello %player:name%", "say str is $str-arg-name")));
            this.add(new BundleCommandNode("This command is used to test the `greedy string` argument type.", new CommandRequirementDescriptor(4, null), "my-command a-command-with-a-greedy-string <int int-arg-name> first-literal [greedy-string greedy-string-arg-name this is the default value]", List.of("say hello %player:name%", "say int is $int-arg-name", "say str is $greedy-string-arg-name")));
            this.add(new BundleCommandNode("This command is used to test the `literal argument`, `required argument` and `optional argument`.", new CommandRequirementDescriptor(4, null), "my-command a-command-with-all-types-of-args <int int-arg-name> first-literal [str str-arg-name the default value can contains placeholder %player:name% in %world:name%]", List.of("say hello %player:name%", "say int is $int-arg-name", "say str is $str-arg-name")));

            /* Register gamemode switcher commands. */
            this.add(new BundleCommandNode("This command is an alias for `/gamemode`", new CommandRequirementDescriptor(4, null), "gm <gamemode gamemode-arg>", List.of("run as player %player:name% gamemode $gamemode-arg")));
            this.add(new BundleCommandNode("This command is an alias for `/gamemode creative`", new CommandRequirementDescriptor(4, null), "gmc", List.of("run as player %player:name% gamemode creative")));
            this.add(new BundleCommandNode("This command is an alias for `/gamemode survival`", new CommandRequirementDescriptor(4, null), "gms", List.of("run as player %player:name% gamemode survival")));
            this.add(new BundleCommandNode("This command is an alias for `/gamemode spectator`", new CommandRequirementDescriptor(4, null), "gmsp", List.of("run as player %player:name% gamemode spectator")));

            /* Register weather changer commands. */
            this.add(new BundleCommandNode("This command is an alias for `/weather clear`", new CommandRequirementDescriptor(4, null), "sun", List.of("run as player %player:name% weather clear")));
            this.add(new BundleCommandNode("This command is an alias for `/weather rain`", new CommandRequirementDescriptor(4, null), "rain", List.of("run as player %player:name% weather rain")));
            this.add(new BundleCommandNode("This command is an alias for `/weather thunder`", new CommandRequirementDescriptor(4, null), "thunder", List.of("run as player %player:name% weather thunder")));

            /* Register time changer commands. */
            this.add(new BundleCommandNode("This command is an alias for `/time set day`", new CommandRequirementDescriptor(4, null), "day", List.of("run as player %player:name% time set day")));
            this.add(new BundleCommandNode("This command is an alias for `/time set night`", new CommandRequirementDescriptor(4, null), "night", List.of("run as player %player:name% time set night")));
            this.add(new BundleCommandNode("This command is an alias for `/time set midnight`", new CommandRequirementDescriptor(4, null), "midnight", List.of("run as player %player:name% time set midnight")));
            this.add(new BundleCommandNode("This command is an alias for `/time set noon`", new CommandRequirementDescriptor(4, null), "noon", List.of("run as player %player:name% time set noon")));

            /* Register NBT commands. */
            this.add(new BundleCommandNode("This command is an alias for `/data get entity`", new CommandRequirementDescriptor(4, null), "nbt entity <entity target>", List.of("run as fake-op %player:name% data get entity $target")));
            this.add(new BundleCommandNode("This command is an alias for `/data get block`", new CommandRequirementDescriptor(4, null), "nbt block <block-pos target>", List.of("run as fake-op %player:name% data get block $target")));
            this.add(new BundleCommandNode("This command is an alias for `/data get entity @s SelectedItem`", new CommandRequirementDescriptor(4, null), "nbt item", List.of("run as fake-op %player:name% data get entity %player:name% SelectedItem")));

            /* Register easter eggs commands. */
            this.add(new BundleCommandNode("This command is an easter egg for `/plugins` in Bukkit.", new CommandRequirementDescriptor(0, null), "plugins", List.of("send-message %player:name% Server Plugins (0): ")));
            this.add(new BundleCommandNode("This command is an easter egg for `/icanhasbukkit` in Bukkit.", new CommandRequirementDescriptor(0, null), "icanhasbukkit", List.of(
                "send-message %player:name% <i>Checking version, please wait..."
                , "delay 2 send-message %player:name% This server is running Bukkit version (MC: %server:version%)"
                , "delay 3 send-message %player:name% <green>You are running the latest version")));

            /* Register utility commands. */
            this.add(new BundleCommandNode("This command summons an entity with specified entity type, with an initial motion.", new CommandRequirementDescriptor(4, null), "shoot <entity-type entity-type-arg-name>", List.of("execute as %player:name% run summon $entity-type-arg-name ~ ~1 ~ {ExplosionPower:4,Motion:[3.0,0.0,0.0]}")));
            this.add(new BundleCommandNode("This command summons a lightning_bolt entity.", new CommandRequirementDescriptor(4, null), "strike", List.of("execute as %player:name% at @s run summon lightning_bolt ^ ^ ^32")));
            this.add(new BundleCommandNode("This command applies the `unbreakable` enchantment for the item in hand.", new CommandRequirementDescriptor(4, null), "unbreakable", List.of("run as player %player:name% enchant %player:name% minecraft:unbreaking")));
            this.add(new BundleCommandNode("This command modifies your movement_speed attribute.", new CommandRequirementDescriptor(4, null), "move-speed set <double double-arg>", List.of("run as player %player:name% attribute %player:name% minecraft:generic.movement_speed base set $double-arg")));
            this.add(new BundleCommandNode("This command modifies your movement_speed attribute.", new CommandRequirementDescriptor(4, null), "move-speed reset", List.of("run as player %player:name% attribute %player:name% minecraft:generic.movement_speed base set 0.10000000149011612")));
            this.add(new BundleCommandNode("This command will introduce yourself to others.", new CommandRequirementDescriptor(4, null), "introduce-me", List.of("run as fake-op %player:name% me i am %player:name%")));
            this.add(new BundleCommandNode("This command will roll a random dice.", new CommandRequirementDescriptor(4, null), "dice", List.of("say %player:name% just roll out %fuji:random 1 6% points.")));
            this.add(new BundleCommandNode("This command will give `all` recipes to the player.", new CommandRequirementDescriptor(4, null), "obtain-all-recipes", List.of("run as fake-op %player:name% recipe give %player:name% *")));
            this.add(new BundleCommandNode("This command will give the skull of specified player.", new CommandRequirementDescriptor(4, null),
                "skull <offline-player offline-player-arg>", List.of("give %player:name% minecraft:player_head[minecraft:profile=$offline-player-arg]")));
            this.add(new BundleCommandNode("This command will print the UUID of specified player.", new CommandRequirementDescriptor(4, null),
                "uuid <player target>", List.of("run as fake-op $target send-message %player:name% <yellow>The UUID of player $target is %fuji:escape player:uuid 2%")));

        }
    };

}

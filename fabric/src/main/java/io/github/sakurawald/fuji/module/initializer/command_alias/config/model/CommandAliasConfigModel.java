package io.github.sakurawald.fuji.module.initializer.command_alias.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.command.structure.CommandRequirementDescriptor;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.command_alias.structure.CommandAliasEntry;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommandAliasConfigModel {

    @Document(id = 1751826293492L, value = """
        Defined `alias` for `existing commands`.
        """)
    @SerializedName(value = "alias_commands", alternate = {"alias", "aliases"})
    List<CommandAliasEntry> aliasCommands = new ArrayList<>() {
        {
            /* Level 0 commands. */
            this.add(new CommandAliasEntry(true, "Create an alias command from `/r` into `/reply` command.", new CommandRequirementDescriptor(0, null), List.of("r"), List.of("reply")));
            this.add(new CommandAliasEntry(true, "Create an alias command from `/display` into `/chat display` command.", new CommandRequirementDescriptor(0, null), List.of("display"), List.of("chat", "display")));

            /* Level 4 commands. */
            this.add(new CommandAliasEntry(true, "Create an alias command from `/sudo` into `/run as fake-op` command.", new CommandRequirementDescriptor(4, null), List.of("sudo"), List.of("run", "as", "fake-op")));
            this.add(new CommandAliasEntry(true, "Create an alias command from `/wb` into `/workbench` command.", new CommandRequirementDescriptor(4, null), List.of("wb"), List.of("workbench")));
            this.add(new CommandAliasEntry(true, "Create an alias command from `/invsee` into `/view inv` command.", new CommandRequirementDescriptor(4, null), List.of("invsee"), List.of("view", "inv")));
            this.add(new CommandAliasEntry(true, "Create an alias command from `/endersee` into `/view ender` command.", new CommandRequirementDescriptor(4, null), List.of("endersee"), List.of("view", "ender")));
        }
    };
}

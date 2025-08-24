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
    @SerializedName(value = "aliases", alternate = "alias")
    List<CommandAliasEntry> aliases = new ArrayList<>() {
        {
            this.add(new CommandAliasEntry("Create an alias command from `/r` into `/reply` command.", new CommandRequirementDescriptor(0, null), List.of("r"), List.of("reply")));
            this.add(new CommandAliasEntry("Create an alias command from `/display` into `/chat display` command.", new CommandRequirementDescriptor(0, null), List.of("display"), List.of("chat", "display")));
            this.add(new CommandAliasEntry("Create an alias command from `/sudo` into `/run as fake-op` command.", new CommandRequirementDescriptor(4, null), List.of("sudo"), List.of("run", "as", "fake-op")));
        }
    };
}

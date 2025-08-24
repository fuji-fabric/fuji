package io.github.sakurawald.fuji.module.initializer.command_alias.structure;

import io.github.sakurawald.fuji.core.command.structure.CommandRequirementDescriptor;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import lombok.NoArgsConstructor;

@SuppressWarnings("unused")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandAliasEntry {

    boolean enable = true;

    String document = "This is the document string for this command.";

    CommandRequirementDescriptor requirement = new CommandRequirementDescriptor(4, null);

    @Document(id = 1751826295900L, value = """
        The `path` of `source command`.
        """)
    List<String> from;

    @Document(id = 1751826299340L, value = """
        The `path` of `destination command`.
        """)
    List<String> to;
}

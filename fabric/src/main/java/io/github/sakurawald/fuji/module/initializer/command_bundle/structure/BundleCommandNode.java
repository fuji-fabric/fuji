package io.github.sakurawald.fuji.module.initializer.command_bundle.structure;

import io.github.sakurawald.fuji.core.annotation.Document;
import io.github.sakurawald.fuji.core.command.structure.CommandRequirementDescriptor;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BundleCommandNode {

    @Document("""
        The requirement to use this `bundle command`.
        """)
    CommandRequirementDescriptor requirement;

    @Document("""
        The `syntax pattern` for this `bundle command`.
        """)
    String pattern;

    @Document("""
        The `body` of this `bundle command`.

        The `body` is a list of commands.
        It will be executed as console.
        """)
    List<String> bundle;
}

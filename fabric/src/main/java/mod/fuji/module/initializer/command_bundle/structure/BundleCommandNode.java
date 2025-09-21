package mod.fuji.module.initializer.command_bundle.structure;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.command.structure.CommandRequirementDescriptor;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BundleCommandNode {

    @Document(id = 1753076962665L, value = """
        The `document` for this `bundle command`. This field can be `null`.
        """)
    String document;

    @Document(id = 1751826346275L, value = """
        The requirement to use this `bundle command`.
        """)
    CommandRequirementDescriptor requirement;

    @Document(id = 1751826349060L, value = """
        The `syntax pattern` for this `bundle command`.
        """)
    String pattern;

    @Document(id = 1751826352098L, value = """
        The `body` of this `bundle command`.

        The `body` is a list of commands.
        It will be executed as console.
        """)
    List<String> bundle;
}

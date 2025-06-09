package io.github.sakurawald.module.initializer.command_bundle.structure;

import io.github.sakurawald.core.command.structure.CommandRequirementDescriptor;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BundleCommandNode {

    CommandRequirementDescriptor requirement;

    String pattern;

    List<String> bundle;
}

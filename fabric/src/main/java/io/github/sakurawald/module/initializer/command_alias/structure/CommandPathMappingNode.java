package io.github.sakurawald.module.initializer.command_alias.structure;

import lombok.Data;

import java.util.List;

@SuppressWarnings("unused")
@Data
public class CommandPathMappingNode {
    final List<String> from;
    final List<String> to;
}

package io.github.sakurawald.fuji.module.initializer.command_alias.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.Data;

import java.util.List;

@SuppressWarnings("unused")
@Data
public class CommandPathMappingNode {
    @Document(id = 1751826295900L, value = """
        The `path` of `source command`.
        """)
    final List<String> from;

    @Document(id = 1751826299340L, value = """
        The `path` of `destination command`.
        """)
    final List<String> to;
}

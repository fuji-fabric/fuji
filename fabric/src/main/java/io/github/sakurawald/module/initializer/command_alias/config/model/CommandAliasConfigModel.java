package io.github.sakurawald.module.initializer.command_alias.config.model;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.module.initializer.command_alias.structure.CommandPathMappingNode;

import java.util.ArrayList;
import java.util.List;

public class CommandAliasConfigModel {

    @Document("""
        Defined `alias` for `existing commands`.
        """)
    public List<CommandPathMappingNode> alias = new ArrayList<>() {
        {
            this.add(new CommandPathMappingNode(List.of("r"), List.of("reply")));
            this.add(new CommandPathMappingNode(List.of("sudo"), List.of("run", "as", "fake-op")));
            this.add(new CommandPathMappingNode(List.of("i", "want", "to", "modify", "chat"), List.of("chat", "style")));
            this.add(new CommandPathMappingNode(List.of("display"), List.of("chat", "display")));
        }
    };
}

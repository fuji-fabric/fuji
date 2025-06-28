package io.github.sakurawald.fuji.module.initializer.command_warmup.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.manager.impl.bossbar.structure.Interruptible;
import io.github.sakurawald.fuji.core.structure.Tag;
import lombok.Data;

@Data
public class CommandWarmupNode {

    final Tag tag;

    @Document("""
        The `target command` and `warmup time in ms`.
        """)
    final Command command;

    final Interruptible interruptible;

    @Data
    public static class Command {
        @Document("""
            The `regex` expression used to match the `target command`.
            """)
        final String regex;

        @Document("""
            The `warmup time` in ms.
            """)
        final int ms;
    }

    public static CommandWarmupNode make(Command command, Interruptible interruptible) {
        return new CommandWarmupNode(new Tag(), command, interruptible);
    }

}

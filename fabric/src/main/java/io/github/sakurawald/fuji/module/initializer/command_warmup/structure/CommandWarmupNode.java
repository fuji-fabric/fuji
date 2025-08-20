package io.github.sakurawald.fuji.module.initializer.command_warmup.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.manager.impl.bossbar.structure.Interruptible;
import io.github.sakurawald.fuji.core.structure.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandWarmupNode {

    Tag tag;

    @Document(id = 1751826877229L, value = """
        The `target command` and `warmup time in ms`.
        """)
    Command command;

    Interruptible interruptible;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Command {
        @Document(id = 1751826879068L, value = """
            The `regex` expression used to match the `target command`.
            """)
        String regex;

        @Document(id = 1751826881411L, value = """
            The `warmup time` in ms.
            """)
        int ms;
    }

    public static CommandWarmupNode make(Command command, Interruptible interruptible) {
        return new CommandWarmupNode(new Tag(), command, interruptible);
    }

}

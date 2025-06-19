package io.github.sakurawald.module.initializer.command_warmup.structure;

import io.github.sakurawald.core.manager.impl.bossbar.structure.Interruptible;
import io.github.sakurawald.core.structure.Tag;
import lombok.Data;

@Data
public class CommandWarmupNode {

    final Tag tag;
    final Command command;
    final Interruptible interruptible;

    @Data
    public static class Command {
        final String regex;
        final int ms;
    }

    public static CommandWarmupNode make(Command command, Interruptible interruptible) {
        return new CommandWarmupNode(new Tag(), command, interruptible);
    }

}

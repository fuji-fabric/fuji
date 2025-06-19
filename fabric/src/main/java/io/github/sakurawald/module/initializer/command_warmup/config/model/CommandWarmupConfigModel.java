package io.github.sakurawald.module.initializer.command_warmup.config.model;

import io.github.sakurawald.core.manager.impl.bossbar.structure.Interruptible;
import io.github.sakurawald.module.initializer.command_warmup.structure.CommandWarmupNode;

import java.util.ArrayList;
import java.util.List;

public class CommandWarmupConfigModel {

    public boolean warn_for_move = true;

    public List<CommandWarmupNode> entries = new ArrayList<>() {
        {
            this.add(CommandWarmupNode.make(new CommandWarmupNode.Command("back", 3 * 1000), new Interruptible(true, 3, true, true)));
            this.add(CommandWarmupNode.make(new CommandWarmupNode.Command("heal", 1000), new Interruptible(true, 3, true, true)));
        }
    };
}

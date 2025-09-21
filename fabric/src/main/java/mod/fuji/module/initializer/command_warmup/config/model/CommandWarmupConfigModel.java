package mod.fuji.module.initializer.command_warmup.config.model;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.manager.impl.bossbar.structure.Interruptible;
import mod.fuji.module.initializer.command_warmup.structure.CommandWarmupNode;

import java.util.ArrayList;
import java.util.List;

public class CommandWarmupConfigModel {

    @Document(id = 1751826870516L, value = """
        Should we send a warning message for no movement?
        """)
    public boolean warn_for_move = true;

    @Document(id = 1751826873894L, value = """
        Defined `warmup` rules.
        """)
    @SerializedName(value = "rules", alternate = "entries")
    public List<CommandWarmupNode> rules = new ArrayList<>() {
        {
            this.add(CommandWarmupNode.make(new CommandWarmupNode.Command("back", 3 * 1000), new Interruptible(true, 3, true, true)));
            this.add(CommandWarmupNode.make(new CommandWarmupNode.Command("heal", 1000), new Interruptible(true, 3, true, true)));
        }
    };
}

package io.github.sakurawald.module.initializer.command_scheduler.structure;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.command.executor.CommandExecutor;
import io.github.sakurawald.core.command.structure.ExtendedCommandSource;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Random;

@Data
@AllArgsConstructor
public class Job {

    String name;
    boolean enable;
    @SerializedName(value = "left_times", alternate = "left_trigger_times")
    int leftTimes;
    List<String> crons;
    List<List<String>> commands_list;

    // for implement simplification, the job will always be scheduled, and the trigger() will always be called.
    public void trigger() {

        /* process enable */
        if (!this.enable) return;

        /* process left trigger times */
        if (leftTimes <= 0) {
            return;
        }
        leftTimes--;

        /* execute commands */
        List<String> commands = this.commands_list.get(new Random().nextInt(this.commands_list.size()));
        LogUtil.info("execute commands in job `{}`: {}", this.getName(), commands);

        ServerHelper.getServer().executeSync(() -> CommandExecutor.execute(ExtendedCommandSource.asConsole(ServerHelper.getServer().getCommandSource()), commands));
    }
}

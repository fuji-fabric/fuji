package io.github.sakurawald.fuji.module.initializer.command_scheduler.structure;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.module.initializer.command_scheduler.CommandSchedulerInitializer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Random;

@Data
@AllArgsConstructor
public class Job {

    @Document(id = 1751826743418L, value = """
        The `unique` name of this `job`.
        """)
    String name;

    boolean enable;

    @Document(id = 1751826745342L, value = """
        Allowed left times to run.
        """)
    @SerializedName(value = "left_times", alternate = "left_trigger_times")
    int leftTimes;

    @Document(id = 1751826746949L, value = """
        Defined `cron` expression list.

        Any met `cron` expression can `trigger` this `job`.
        """)
    List<String> crons;

    @Document(id = 1751826749083L, value = """
        The commands to execute when the `job` is `triggered`.
        """)
    List<List<String>> commands_list;

    // for implement simplification, the job will always be scheduled, and the trigger() will always be called.
    public void tryTrigger() {
        /* Filter for enable option. */
        if (!this.enable) return;

        /* Filter for leftTimes option. */
        if (leftTimes <= 0) {
            return;
        }
        leftTimes--;

        /* Save storage. */
        CommandSchedulerInitializer.scheduler.writeStorage();

        /* Execute specified commands. */
        List<String> commands = this.commands_list.get(new Random().nextInt(this.commands_list.size()));
        LogUtil.info("Execute commands in job `{}`: {}", this.getName(), commands);
        ServerHelper.executeSync(() -> CommandExecutor.execute(ExtendedCommandSource.asConsole(ServerHelper.getServer().getCommandSource()), commands));
    }
}

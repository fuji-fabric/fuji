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
    @SerializedName(value = "remaining_runs", alternate = {"left_trigger_times", "left_times"})
    int remainingRuns;

    @Document(id = 1751826746949L, value = """
        Defined `cron` expression list.

        Any met `cron` expression can `trigger` this `job`.
        """)
        @SerializedName(value = "schedules", alternate = "crons")
    List<String> schedules;

    @Document(id = 1751826749083L, value = """
        The commands to execute when the `job` is `triggered`.
        """)
    @SerializedName(value = "commands_groups", alternate = "commands_list")
    List<List<String>> commands_groups;

    // for implement simplification, the job will always be scheduled, and the trigger() will always be called.
    public void tryTrigger() {
        /* Filter for enable option. */
        if (!this.enable) return;

        /* Filter for leftTimes option. */
        if (remainingRuns <= 0) {
            return;
        }
        remainingRuns--;

        /* Save storage. */
        CommandSchedulerInitializer.scheduler.writeStorage();

        /* Execute specified commands. */
        List<String> commands = this.commands_groups.get(new Random().nextInt(this.commands_groups.size()));
        LogUtil.info("Execute commands in job `{}`: {}", this.getName(), commands);
        ServerHelper.executeSync(() -> CommandExecutor.execute(ExtendedCommandSource.asConsole(ServerHelper.getServer().getCommandSource()), commands));
    }
}

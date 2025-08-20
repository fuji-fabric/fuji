package io.github.sakurawald.fuji.module.initializer.command_scheduler.structure;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.auxiliary.RandomUtil;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.module.initializer.command_scheduler.CommandSchedulerInitializer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandSchedulerJobDescriptor {

    boolean enable;

    @Document(id = 1751826743418L, value = """
        The `unique` name of this `job`.
        """)
    String name;

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

    @ForDeveloper("For implement simplification, the job will always be scheduled, and the trigger() will always be called.")
    public void tryTrigger() {
        /* Verify the enable property. */
        if (!this.enable) {
            return;
        }

        /* Verify the remaining runs property. */
        if (remainingRuns <= 0) {
            return;
        }
        remainingRuns--;

        /* Update storage. */
        CommandSchedulerInitializer.scheduler.writeStorage();

        /* Execute specified commands. */
        List<String> commands = RandomUtil.drawList(this.commands_groups);
        LogUtil.info("Execute commands in job `{}`: {}", this.getName(), commands);
        ServerHelper.executeSync(() -> CommandExecutor.execute(ExtendedCommandSource.asConsole(ServerHelper.getServer().getCommandSource()), commands));
    }
}

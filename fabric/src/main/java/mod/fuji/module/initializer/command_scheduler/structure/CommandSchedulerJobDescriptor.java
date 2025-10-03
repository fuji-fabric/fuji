package mod.fuji.module.initializer.command_scheduler.structure;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.auxiliary.RandomUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.command.executor.CommandExecutor;
import mod.fuji.core.command.executor.structure.ExtendedCommandSource;
import mod.fuji.core.document.annotation.ForDeveloper;
import mod.fuji.module.initializer.command_scheduler.CommandSchedulerInitializer;
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
    @SerializedName(value = "command_groups", alternate = {"commands_list", "commands_groups"})
    List<List<String>> command_groups;

    /**
 * For implement simplification, the job will always be scheduled, and the trigger() will always be called.
 **/
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
        List<String> commands = RandomUtil.drawList(this.command_groups);
        LogUtil.info("Execute commands in job `{}`: {}", this.getName(), commands);
        ServerHelper.executeSync(() -> CommandExecutor.executeBatch(ExtendedCommandSource.asConsole(CommandHelper.Source.getConsoleCommandSource()), commands));
    }
}

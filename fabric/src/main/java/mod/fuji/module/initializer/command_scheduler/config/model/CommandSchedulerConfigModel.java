package mod.fuji.module.initializer.command_scheduler.config.model;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.job.JobManager;
import mod.fuji.module.initializer.command_scheduler.structure.CommandSchedulerJobDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandSchedulerConfigModel {

    @SerializedName(value = "jobs", alternate = "scheduleJobs")
    public @NotNull List<CommandSchedulerJobDescriptor> jobs = new ArrayList<>() {
        {
            this.add(new CommandSchedulerJobDescriptor(true, "example_job", 1024, List.of(
                JobManager.CRON_EVERY_THREE_MINUTES
                , JobManager.CRON_EVERY_FIVE_MINUTES
            ),
                List.of(
                    List.of(
                        "send-broadcast Group 1 Command 1 -> This is the first group of commands.",
                        "send-broadcast Group 1 Command 2 -> When job is fired, a random command group will be picked.",
                        "send-broadcast Group 1 Command 3 -> Commands in a group run in order from top to bottom.",
                        "send-broadcast Group 1 Command 4 -> You can execute `/command-schedule trigger` to `run` a `job` directly."
                    ),
                    List.of(
                        "send-broadcast Group 2 Command 1 -> This is the second group of commands",
                        "send-broadcast Group 2 Command 2 -> You can use `/fuji` command to inspect the `next fire dates` of each job.",
                        "send-broadcast Group 2 Command 3 -> The schedule for this job is described using 2 cron expression: one says `every 3 minutes`, another says `every 5 minutes`"
                    )
                )));
        }
    };
}

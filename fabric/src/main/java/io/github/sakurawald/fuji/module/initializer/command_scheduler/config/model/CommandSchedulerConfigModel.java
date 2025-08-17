package io.github.sakurawald.fuji.module.initializer.command_scheduler.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.command_scheduler.structure.Job;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandSchedulerConfigModel {

    @Document(id = 1751826740777L, value = """
        Defined `scheduler` entry.
        """)
    @SerializedName(value = "jobs", alternate = "scheduleJobs")
    public @NotNull List<Job> jobs = new ArrayList<>() {
        {
            this.add(new Job("example_job", true, 1024, List.of("0 0 * ? * *"),
                List.of(
                    List.of(
                        "send-broadcast Group 1.1 -> This is the first group of commands.",
                        "send-broadcast Group 1.2 -> When job is fired, a random command group will be picked.",
                        "send-broadcast Group 1.3 -> Commands in a group run in order from top to bottom.",
                        "send-broadcast Group 1.4 -> You can execute `/command-schedule trigger` to `run` a `job` directly."
                    ),
                    List.of(
                        "send-broadcast Group 2.1 -> This is the second group of commands",
                        "send-broadcast Group 2.2 -> You can use `/fuji` command to inspect the `next fire dates` of each job."
                    )
                )));
        }
    };
}

package io.github.sakurawald.module.initializer.command_scheduler.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.module.initializer.command_scheduler.structure.Job;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandSchedulerConfigModel {

    @SerializedName(value = "jobs", alternate = "scheduleJobs")
    public @NotNull List<Job> jobs = new ArrayList<>() {
        {
            this.add(new Job("example_job", false, 3, List.of("0 0 * ? * *"),
                List.of(
                    List.of("send-broadcast nobody gets the gift"),
                    List.of("send-broadcast all players get the gift!", "give @a minecraft:apple 16")
                )));
        }
    };
}

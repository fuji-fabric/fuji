package mod.fuji.core.config.job;

import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.job.abst.CronJob;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.function.Supplier;

@Document(id = 1751823659459L, value = """
    This `job` is used to `write data` from `memory` into `storage`.

    NOTE: <red>If you modify the `file` in `disk`, then you need to issue `/fuji reload` as soon as possible.</red>
    NOTE: <red>The `disk` will be `overridden` when `fire` this job.</red>
    """)
@NoArgsConstructor
public class ConfigurationHandlerWriteStorageJob extends CronJob {

    public ConfigurationHandlerWriteStorageJob(String jobName, JobDataMap jobDataMap, Supplier<String> cronSupplier) {
        super(null, jobName, jobDataMap, cronSupplier, false);
    }

    @Override
    public void execute(@NotNull JobExecutionContext context) {
        BaseConfigurationHandler<?> configHandler = (BaseConfigurationHandler<?>) context
            .getJobDetail()
            .getJobDataMap()
            .get(BaseConfigurationHandler.class.getName());

        // NOTE: The debug() function is not guaranteed to be printed while shutdown the jvm.
        LogUtil.debug("Save configuration file: {}", configHandler.getFilePath());

        // Write storage when stopping the server.
        configHandler.writeStorage();
    }
}

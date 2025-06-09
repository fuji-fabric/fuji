package io.github.sakurawald.core.config.job;

import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.job.abst.CronJob;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.function.Supplier;

@NoArgsConstructor
public class ConfigurationHandlerSaverJob extends CronJob {

    public ConfigurationHandlerSaverJob(String jobName, JobDataMap jobDataMap, Supplier<String> cronSupplier) {
        super(null, jobName, jobDataMap, cronSupplier);
    }

    @Override
    public void execute(@NotNull JobExecutionContext context) {
        // the debug() function is not guaranteed to be printed while shutdown the jvm.
        BaseConfigurationHandler<?> configHandler = (BaseConfigurationHandler<?>) context.getJobDetail().getJobDataMap().get(BaseConfigurationHandler.class.getName());
        LogUtil.debug("save configuration file: {}", configHandler.getPath());

        configHandler.writeStorage();
    }
}

package io.github.sakurawald.fuji.module.initializer.fuji.structure;

import io.github.sakurawald.fuji.core.document.interfaces.SourceModuleGetter;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Trigger;

import java.util.List;

@Data
@AllArgsConstructor
public class JobDescriptor implements SourceModuleGetter {
    public final JobDetail jobDetail;
    List<? extends Trigger> triggersOfJob;

    @Override
    public String getSourceModule() {
        JobDetail jobDetail = this.jobDetail;
        Class<? extends Job> jobClass = jobDetail.getJobClass();
        return ModuleManager.computeJoinedModulePath(jobClass.getName());
    }

}

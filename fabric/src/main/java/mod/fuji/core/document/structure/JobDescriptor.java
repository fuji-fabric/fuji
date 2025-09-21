package mod.fuji.core.document.structure;

import mod.fuji.core.document.interfaces.SourceModuleGetter;
import mod.fuji.core.manager.impl.module.ModulePathResolver;
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

        /* Try to find the source module from the data map. */
        Object specifiedSourceModule = jobDetail.getJobDataMap().get(SPECIFIED_SOURCE_MODULE_KEY);
        if (specifiedSourceModule != null) {
            return specifiedSourceModule.toString();
        }

        /* No source module is specified, try to compute it from the job class. */
        Class<? extends Job> jobClass = jobDetail.getJobClass();
        return ModulePathResolver.computeModulePathString(jobClass.getName());
    }

}

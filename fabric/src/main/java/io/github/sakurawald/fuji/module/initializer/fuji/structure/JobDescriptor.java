package io.github.sakurawald.fuji.module.initializer.fuji.structure;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.quartz.JobDetail;
import org.quartz.Trigger;

import java.util.List;

@Data
@AllArgsConstructor
public class JobDescriptor {
    public final JobDetail jobDetail;
    List<? extends Trigger> triggersOfJob;
}

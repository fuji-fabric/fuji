package io.github.sakurawald.fuji.module.initializer.document.builder;

import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.document.auxiliary.DocumentUtil;
import io.github.sakurawald.fuji.core.document.structure.JobDescriptor;
import java.util.List;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.quartz.Job;
import org.quartz.SchedulerException;

public class ModuleJobsDocumentBuilder extends DocumentBuilder {

    @SneakyThrows(SchedulerException.class)
    @Override
    public void build(@NotNull DocumentBuilderContext documentBuilderContext) {
        List<JobDescriptor> jobs = DocumentUtil
            .getJobDescriptors()
            .stream()
            .filter(it -> it.getSourceModule().equals(documentBuilderContext.getModulePathString()))
            .toList();

        if (!jobs.isEmpty()) {
            documentBuilderContext
                .getDocumentBuilder()
                .append("## Jobs")
                .append(System.lineSeparator());

            jobs.forEach(it -> build(documentBuilderContext, it));
        }

    }

    private void build(@NotNull DocumentBuilderContext documentBuilderContext, @NotNull JobDescriptor jobDescriptor) {

        Class<? extends Job> jobClass = jobDescriptor.getJobDetail().getJobClass();
        String jobSimpleClassName = ReflectionUtil.getSimpleClassName(jobClass);
        documentBuilderContext
            .getDocumentBuilder()
            .append(":::job").append(System.lineSeparator())
            .append("- Job Name: `%s`".formatted(jobSimpleClassName)).append(System.lineSeparator());

        DocumentUtil
            .getClassDocumentString(null, jobClass)
            .ifPresent(jobDocument -> {
                documentBuilderContext
                    .getDocumentBuilder()
                    .append("- Document: %s".formatted(jobDocument));
            });

        documentBuilderContext
            .getDocumentBuilder()
            .append(":::").append(System.lineSeparator());

    }
}

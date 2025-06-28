package io.github.sakurawald.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.PagedGui;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.fuji.module.initializer.fuji.structure.JobDescriptor;
import lombok.SneakyThrows;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class JobsInspectionGui extends PagedGui<JobDescriptor> {

    public JobsInspectionGui(@Nullable SimpleGui parent, ServerPlayerEntity player, @NotNull List<JobDescriptor> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "fuji.inspect.jobs.gui.title"), entities, pageIndex);
    }

    @Override
    protected PagedGui<JobDescriptor> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<JobDescriptor> entities, int pageIndex) {
        return new JobsInspectionGui(parent, player, entities, pageIndex);
    }

    @SneakyThrows
    public static JobsInspectionGui inspectAll(SimpleGui parent, ServerPlayerEntity player) {
        List<JobDescriptor> entities = new ArrayList<>();

        /* Get all jobs. */
        Scheduler scheduler = Managers.getScheduleManager().getScheduler();

        GroupMatcher<JobKey> jobKeyGroupMatcher = GroupMatcher.anyJobGroup();
        Set<JobKey> jobKeys = scheduler.getJobKeys(jobKeyGroupMatcher);
        for (JobKey jobKey : jobKeys) {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(jobKey);
            entities.add(new JobDescriptor(jobDetail, triggersOfJob));
        }

        /* Make the GUI. */
        return new JobsInspectionGui(parent, player, entities, 0);
    }

    private List<Date> getFireDates(Trigger trigger) {
        List<Date> fireDates = new ArrayList<>();

        Date baseTime = trigger.getPreviousFireTime();
        fireDates.add(baseTime);

        for (int i = 0; i < 5; i++) {
            Date fireTimeAfter = trigger.getFireTimeAfter(baseTime);
            fireDates.add(fireTimeAfter);
            baseTime = fireTimeAfter;
        }

        return fireDates;
    }

    @Override
    protected GuiElementInterface toGuiElement(JobDescriptor entity) {
        JobDetail jobDetail = entity.jobDetail;
        JobKey jobKey = jobDetail.getKey();
        Class<? extends Job> jobClass = jobDetail.getJobClass();
        String fromModule = ModuleManager.computeModulePathAsString(jobClass.getName());

        List<Text> lore = new ArrayList<>();
        lore.add(TextHelper.getTextByKey(getPlayer(), "from_module", fromModule));
        lore.add(TextHelper.getTextByKey(getPlayer(), "job.job_group", jobKey.getGroup()));
        lore.add(TextHelper.getTextByKey(getPlayer(), "job.job_name", jobKey.getName()));
        entity.getTriggersOfJob()
                .forEach(trigger -> {
                    lore.add(TextHelper.getTextByKey(getPlayer(), "job.fire_dates"));
                    getFireDates(trigger)
                            .forEach(fireDate -> lore.add(TextHelper.getTextByKey(getPlayer(), "job.fire_dates.entry", fireDate)));


                });

        GuiElementBuilder guiElementBuilder = new GuiElementBuilder()
                .setItem(Items.CLOCK)
                .setName(TextHelper.getTextByKey(getPlayer(), "fuji.inspect.jobs.gui.item.name"))
                .setLore(lore);
        return guiElementBuilder.build();
    }

    @Override
    protected boolean filterEntity(JobDescriptor entity, String keyword) {
        return false;
    }
}

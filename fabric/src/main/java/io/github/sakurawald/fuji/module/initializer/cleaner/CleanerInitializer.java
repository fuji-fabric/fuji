package io.github.sakurawald.fuji.module.initializer.cleaner;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.cleaner.config.model.CleanerConfigModel;
import io.github.sakurawald.fuji.module.initializer.cleaner.config.transformer.CleanerV1SchemaTransformer;
import io.github.sakurawald.fuji.module.initializer.cleaner.job.CleanerJob;
import io.github.sakurawald.fuji.module.initializer.cleaner.service.CleanerService;
import net.minecraft.server.command.ServerCommandSource;

@Document(id = 1751826898176L, value = """
    This module provides the `entity` cleaner.
    To remove specified entities automatically.
    """)
@ColorBox(id = 1751870582940L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    You should only use this module to clean some edge-case entity.
    The vanilla Minecraft also has a `cleaner` to remove dropped items.
    In normal case, you can rely on the `vanilla cleaner`.
    But for some special case, you may want to use this module.
    To clean some `annoying dropped items` or even `entities` (`pig` or `boat`).

    Yeah, the `vanilla cleaner` only cleans `dropped items`.
    But this module, allows you to define rules, to clean `dropped items` and `entities`.
    """)
@ColorBox(id = 1751870585373L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    For safety, the `cleaner` will `always ignore` the following types:
    1. player
    2. any block attached entity (e.g. leash_knot)
    3. any vehicle entity (e.g. minecart, boat)
    """)


@CommandNode("cleaner")
@CommandRequirement(level = 4)
public class CleanerInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CleanerConfigModel> config = ObjectConfigurationHandler
        .ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, CleanerConfigModel.class)
        .installTransformer(new CleanerV1SchemaTransformer());

    @Document(id = 1751826901492L, value = "Remove defined `entities` older than the specified `age`.")
    @CommandNode("clean")
    private static int $clean(@CommandSource ServerCommandSource source) {
        CleanerService.cleanEntities();
        return CommandHelper.Return.SUCCESS;
    }

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            CleanerJob cleanerJob = new CleanerJob();
            Managers.getScheduleManager().scheduleJob(cleanerJob);
        });
    }

}

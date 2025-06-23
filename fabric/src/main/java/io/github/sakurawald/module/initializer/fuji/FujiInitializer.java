package io.github.sakurawald.module.initializer.fuji;

import io.github.sakurawald.Fuji;
import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.processor.CommandAnnotationProcessor;
import io.github.sakurawald.core.command.structure.CommandDescriptor;
import io.github.sakurawald.core.config.Configs;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.gui.CommandDescriptorGui;
import io.github.sakurawald.core.job.abst.BaseJob;
import io.github.sakurawald.core.manager.Managers;
import io.github.sakurawald.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.core.structure.Pair;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.fuji.gui.AboutGui;
import io.github.sakurawald.module.initializer.fuji.gui.ArgumentTypeGui;
import io.github.sakurawald.module.initializer.fuji.gui.ConfigurationHandlerGui;
import io.github.sakurawald.module.initializer.fuji.gui.ModulesGui;
import io.github.sakurawald.module.initializer.fuji.gui.RegistryGui;
import io.github.sakurawald.module.initializer.fuji.gui.ServerCommandsGui;
import io.github.sakurawald.module.initializer.fuji.structure.ServerCommandNodeWrapper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Document("""
    Provides `/fuji` command.
    To reload the configs of fuji.
    To inspect states of fuji.
    To discover things of fuji.
    """)
@CommandNode("fuji")
@CommandRequirement(level = 4)
public class FujiInitializer extends ModuleInitializer {

    @CommandNode("reload")
    private static int $reload(@CommandSource ServerCommandSource source) {
        // Reload main-control file.
        Configs.mainControlConfig.readStorage();

        // Reload modules.
        Managers.getModuleManager().reloadModuleInitializers();

        // Reload jobs.
        BaseJob.rescheduleAll();

        TextHelper.sendMessageByKey(source, "reload");
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("user-guide")
    private static int $userGuide(@CommandSource ServerCommandSource source) {
        ModuleManager.printUserGuide();
        TextHelper.sendMessageByKey(source, "fuji.user_guide");
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("about")
    @Document("Open fuji about gui.")
    private static int $about(@CommandSource ServerPlayerEntity player) {
        ModMetadata metadata = FabricLoader.getInstance().getModContainer(Fuji.MOD_ID)
            .orElseThrow(() -> new IllegalStateException("failed to get the metadata of this mod."))
            .getMetadata();

        List<Person> persons = new ArrayList<>();
        persons.addAll(metadata.getAuthors());
        persons.addAll(metadata.getContributors());

        new AboutGui(player, persons, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("debug")
    @Document("Switch the `log_debug_message` option.")
    private static int $debug(@CommandSource ServerCommandSource source) {
        var config = Configs.mainControlConfig.model().core.debug;
        config.log_debug_messages = !config.log_debug_messages;

        TextHelper.sendMessageByKey(source, config.log_debug_messages ? "fuji.debug.on" : "fuji.debug.off");
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("inspect server-commands")
    @Document("Inspect all commands registered in server.")
    private static int $inspectServerCommands(@CommandSource ServerPlayerEntity player) {
        List<ServerCommandNodeWrapper> entities = CommandHelper.getCommandNodes()
            .stream()
            .map(ServerCommandNodeWrapper::new)
            .sorted(Comparator.comparing(ServerCommandNodeWrapper::getPath))
            .toList();
        new ServerCommandsGui(player, entities, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("inspect modules")
    @Document("Inspect all enabled/disabled modules of fuji.")
    private static int $inspectModules(@CommandSource ServerPlayerEntity player) {
        List<Pair<String, Boolean>> list = ModuleManager.MODULE_ENABLE_STATUS
            .entrySet()
            .stream()
            .map(it -> new Pair<>(ModuleManager.joinModulePath(it.getKey()), it.getValue()))
            .sorted(Comparator.comparing(Pair::getKey))
            .toList();

        new ModulesGui(player, list, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("inspect fuji-commands")
    @Document("Inspect all commands registered by fuji.")
    private static int $inspectFujiCommands(@CommandSource ServerPlayerEntity player) {
        List<CommandDescriptor> descriptors = CommandAnnotationProcessor
            .descriptors
            .stream()
            .sorted(Comparator.comparing(CommandDescriptor::getCommandNodePath))
            .toList();

        new CommandDescriptorGui(player, descriptors, 0).open();

        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("inspect argument-types")
    @Document("Inspect all argument types registered by fuji.")
    private static int $inspectCommandArgumentTypes(@CommandSource ServerCommandSource source) {
        List<BaseArgumentTypeAdapter> adapters = BaseArgumentTypeAdapter.getAdapters();

        if (source.isExecutedByPlayer()) {
            new ArgumentTypeGui(source.getPlayer(), adapters, 0).open();
        } else {
            adapters.forEach(adapter -> adapter.getTypeStrings().forEach(typeString -> {
                String typeClass = adapter.getTypeClasses().get(0).getSimpleName();
                String string2types = "%s -> %s".formatted(typeString, typeClass);
                source.sendMessage(Text.literal(string2types));
            }));
        }

        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("inspect configurations")
    @Document("Inspect all loaded configurations files.")
    private static int $inspectConfigurations(@CommandSource ServerPlayerEntity player) {
        List<BaseConfigurationHandler<?>> list = BaseConfigurationHandler.handlers.stream()
            .filter(it -> it instanceof ObjectConfigurationHandler<?>)
            .sorted(Comparator.comparing(BaseConfigurationHandler::getPath))
            .toList();

        new ConfigurationHandlerGui(null, player, list, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("inspect registry")
    @Document("Inspect all registries in server.")
    private static int $inspectRegistry(@CommandSource ServerPlayerEntity player) {
        List<Identifier> staticRegistries = Registries.REGISTRIES.getKeys().stream()
            .map(RegistryKey::getValue)
            .toList();

        List<Identifier> dynamicRegistries = RegistryLoader.DYNAMIC_REGISTRIES.stream().map(it -> it.comp_985().getValue()).toList();

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.addAll(staticRegistries);
        identifiers.addAll(dynamicRegistries);
        identifiers.sort(Comparator.comparing(Identifier::toString));

        new RegistryGui(null, player, true, identifiers, 0).open();
        return CommandHelper.Return.SUCCESS;
    }
}


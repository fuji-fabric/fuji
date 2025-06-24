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
import io.github.sakurawald.core.structure.descriptor.StringDescriptor;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.fuji.gui.AboutGui;
import io.github.sakurawald.module.initializer.fuji.gui.ArgumentTypeInspectionGui;
import io.github.sakurawald.module.initializer.fuji.gui.ConfigurationHandlerInspectionGui;
import io.github.sakurawald.module.initializer.fuji.gui.ModulesInspectionGui;
import io.github.sakurawald.module.initializer.fuji.gui.PermissionsAndMetasInspectionGui;
import io.github.sakurawald.module.initializer.fuji.gui.RegistryInspectionGui;
import io.github.sakurawald.module.initializer.fuji.gui.ServerCommandsInspectionGui;
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

    @Document("""
        Reload all the configuration files in `/fuji inspect configurations`.
        Reload all the `enabled` modules in `/fuji inspect modules`.

        NOTE: You have to `re-start` the server, after you enable/disable a module.
        """)
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

    @Document("Print the user guide of fuji.")
    @CommandNode("user-guide")
    private static int $userGuide(@CommandSource ServerCommandSource source) {
        ModuleManager.printUserGuide();
        TextHelper.sendMessageByKey(source, "fuji.user_guide");
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Open the about GUI.")
    @CommandNode("about")
    private static int $about(@CommandSource ServerPlayerEntity player) {
        ModMetadata metadata = FabricLoader.getInstance().getModContainer(Fuji.MOD_ID)
            .orElseThrow(() -> new IllegalStateException("Failed to get the metadata of this mod."))
            .getMetadata();

        List<Person> persons = new ArrayList<>();
        persons.addAll(metadata.getAuthors());
        persons.addAll(metadata.getContributors());

        new AboutGui(player, persons, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Toggle the debug mode of fuji.")
    @CommandNode("debug")
    private static int $debug(@CommandSource ServerCommandSource source) {
        var config = Configs.mainControlConfig.model().core.debug;
        config.log_debug_messages = !config.log_debug_messages;

        TextHelper.sendMessageByKey(source, config.log_debug_messages ? "fuji.debug.on" : "fuji.debug.off");
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Inspect all commands registered in server.")
    @CommandNode("inspect server-commands")
    private static int $inspectServerCommands(@CommandSource ServerPlayerEntity player) {
        List<ServerCommandNodeWrapper> entities = CommandHelper.getCommandNodes()
            .stream()
            .map(ServerCommandNodeWrapper::new)
            .sorted(Comparator.comparing(ServerCommandNodeWrapper::getPath))
            .toList();
        new ServerCommandsInspectionGui(player, entities, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Inspect all enabled/disabled modules of fuji.")
    @CommandNode("inspect modules")
    private static int $inspectModules(@CommandSource ServerPlayerEntity player) {
        List<Pair<String, Boolean>> list = ModuleManager.MODULE_ENABLE_STATUS
            .entrySet()
            .stream()
            .map(it -> new Pair<>(ModuleManager.joinModulePath(it.getKey()), it.getValue()))
            .sorted(Comparator.comparing(Pair::getKey))
            .toList();

        new ModulesInspectionGui(player, list, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Inspect all commands registered by fuji.")
    @CommandNode("inspect fuji-commands")
    private static int $inspectFujiCommands(@CommandSource ServerPlayerEntity player) {
        List<CommandDescriptor> descriptors = CommandAnnotationProcessor
            .descriptors
            .stream()
            .sorted(Comparator.comparing(CommandDescriptor::getCommandNodePath))
            .toList();

        new CommandDescriptorGui(player, descriptors, 0).open();

        return CommandHelper.Return.SUCCESS;
    }

    @Document("Inspect all argument types registered by fuji.")
    @CommandNode("inspect argument-types")
    private static int $inspectCommandArgumentTypes(@CommandSource ServerCommandSource source) {
        List<BaseArgumentTypeAdapter> adapters = BaseArgumentTypeAdapter.getAdapters();

        if (source.isExecutedByPlayer()) {
            new ArgumentTypeInspectionGui(source.getPlayer(), adapters, 0).open();
        } else {
            adapters.forEach(adapter -> adapter.getTypeStrings().forEach(typeString -> {
                String typeClass = adapter.getTypeClasses().get(0).getSimpleName();
                String string2types = "%s -> %s".formatted(typeString, typeClass);
                source.sendMessage(Text.literal(string2types));
            }));
        }

        return CommandHelper.Return.SUCCESS;
    }

    @Document("Inspect all loaded configurations files.")
    @CommandNode("inspect configurations")
    private static int $inspectConfigurations(@CommandSource ServerPlayerEntity player) {
        List<BaseConfigurationHandler<?>> list = BaseConfigurationHandler.CONFIGURATION_HANDLERS.stream()
            .filter(it -> it instanceof ObjectConfigurationHandler<?>)
            .sorted(Comparator.comparing(BaseConfigurationHandler::getPath))
            .toList();

        new ConfigurationHandlerInspectionGui(null, player, list, 0)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Inspect all registries in server.")
    @CommandNode("inspect registry")
    private static int $inspectRegistry(@CommandSource ServerPlayerEntity player) {
        List<Identifier> staticRegistries = Registries.REGISTRIES.getKeys().stream()
            .map(RegistryKey::getValue)
            .toList();

        List<Identifier> dynamicRegistries = RegistryLoader.DYNAMIC_REGISTRIES.stream().map(it -> it.comp_985().getValue()).toList();

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.addAll(staticRegistries);
        identifiers.addAll(dynamicRegistries);
        identifiers.sort(Comparator.comparing(Identifier::toString));

        new RegistryInspectionGui(null, player, true, identifiers, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Inspect permissions and metas used by fuji.")
    @CommandNode("inspect permissions-and-metas")
    private static int $inspectPermissionsAndMetas(@CommandSource ServerPlayerEntity player) {
        List<StringDescriptor> entities = StringDescriptor.REGISTERED_STRING_DESCRIPTORS
            .stream()
            .sorted(Comparator.comparing(StringDescriptor::getFromModule)
                .thenComparing(StringDescriptor::sortPriority))
            .toList();

        new PermissionsAndMetasInspectionGui(null, player, entities, 0)
            .open();

        return CommandHelper.Return.SUCCESS;
    }
}


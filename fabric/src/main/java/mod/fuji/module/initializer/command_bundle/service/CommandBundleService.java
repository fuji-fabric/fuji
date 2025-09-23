package mod.fuji.module.initializer.command_bundle.service;

import java.util.List;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.descriptor.CommandDescriptor;
import mod.fuji.core.command.processor.CommandAnnotationProcessor;
import mod.fuji.module.initializer.command_bundle.CommandBundleInitializer;
import mod.fuji.module.initializer.command_bundle.structure.BundleCommandDescriptor;
import mod.fuji.module.initializer.command_bundle.structure.BundleCommandNode;
import org.jetbrains.annotations.NotNull;

public class CommandBundleService {

    public static @NotNull List<CommandDescriptor> listRegisteredBundleCommandDescriptors() {
        return CommandAnnotationProcessor.REGISTERED_COMMAND_DESCRIPTORS
            .stream()
            .filter(it -> it instanceof BundleCommandDescriptor)
            .toList();
    }

    private static @NotNull List<BundleCommandNode> listDeclaredBundleCommandDescriptors() {
        return CommandBundleInitializer.config.model().getBundleCommands()
            .stream()
            .filter(BundleCommandNode::isEnable)
            .toList();
    }

    public static void registerAllBundleCommands() {
        listDeclaredBundleCommandDescriptors()
            .stream()
            .map(BundleCommandDescriptor.Maker::from)
            .forEach(CommandDescriptor::register);
        CommandHelper.Tree.updateCommandTree();
    }

    public static void unregisterAllBundleCommands() {
        List<CommandDescriptor> registeredBundleCommandDescriptors = listRegisteredBundleCommandDescriptors();
        registeredBundleCommandDescriptors
            .forEach(CommandDescriptor::unregister);
        CommandHelper.Tree.updateCommandTree();
    }
}

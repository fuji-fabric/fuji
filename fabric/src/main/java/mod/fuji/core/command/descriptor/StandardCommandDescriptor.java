package mod.fuji.core.command.descriptor;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import mod.fuji.Fuji;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.config.model.PermissionModel;
import mod.fuji.core.command.structure.CommandRequirementDescriptor;
import mod.fuji.core.config.Configs;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import org.jetbrains.annotations.NotNull;

public class StandardCommandDescriptor extends CommandDescriptor {

    public static final BaseConfigurationHandler<PermissionModel> permission = ObjectConfigurationHandler.ofPath(Fuji.MOD_CONFIG_PATH.resolve("permission.json"), PermissionModel.class);
    public static final Set<String> LOADED_METHOD_ARGUMENTS_DERIVED_COMMAND_PATHS = new HashSet<>();

    public Optional<String> methodArgumentsDerivedCommandPath = Optional.empty();

    public StandardCommandDescriptor(@NotNull Method method, @NotNull List<CommandArgument> commandArguments) {
        super(method, commandArguments);
    }

    public static void setEffectiveDefaultCommandRequirement(@NotNull StandardCommandDescriptor descriptor) {
        /* Compute the effective default command requirement for the full command path. */
        String fullCommandPath = descriptor.commandArguments
            .stream()
            .filter(CommandArgument::isCommandArgumentSpecifier)
            .map(CommandArgument::getArgumentName)
            .collect(Collectors.joining("."));

        /* Remember this command path. */
        LOADED_METHOD_ARGUMENTS_DERIVED_COMMAND_PATHS.add(fullCommandPath);
        descriptor.methodArgumentsDerivedCommandPath = Optional.of(fullCommandPath);

        /* Get the default command requirement from permission.json file. */
        CommandRequirementDescriptor defaultCommandRequirement = CommandRequirement.computeCommandRequirement(descriptor);
        Map<String, Integer> permissionMap = permission.model()
            .getDefaultLevelPermission()
            .getCommands();
        int effectiveDefaultLevelPermission = permissionMap.computeIfAbsent(fullCommandPath, k -> defaultCommandRequirement.getLevel());

        /* Make the effective command requirement. */
        CommandRequirementDescriptor effectiveDefaultCommandRequirement;
        if (Configs.MAIN_CONTROL_CONFIG.model().core.permission.all_commands_require_level_4_permission_to_use_by_default) {
            effectiveDefaultCommandRequirement = new CommandRequirementDescriptor(4, null);
        } else {
            effectiveDefaultCommandRequirement = new CommandRequirementDescriptor(effectiveDefaultLevelPermission, null);
        }

        /* Apply the requirement for the command arguments. */
        descriptor.commandArguments
            .forEach(it -> it.setRequirement(effectiveDefaultCommandRequirement));
    }

    @Override
    public boolean isConsoleSpammer() {
        return false;
    }
}

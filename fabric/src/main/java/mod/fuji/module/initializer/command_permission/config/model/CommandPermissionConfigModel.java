package mod.fuji.module.initializer.command_permission.config.model;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.command_permission.structure.CommandPermissionTestResult;
import mod.fuji.module.initializer.command_permission.structure.CommandPermissionRule;

import java.util.ArrayList;
import java.util.List;

public class CommandPermissionConfigModel {

    @Document(id = 1751826769068L, value = """
        You can define `rules` to handle `special case`.
        If the `command permission` is `matched` by the `rule`.
        We return the pre-defined `permission test result` directly, without asking the luckperms.

        The `rules` can be used to handle `special case`.
        For example, if you enable the `permission implicitly inheritance` feature, and grant a `root permission` like `fuji.permission.fly`.
        You want to allow the players to use `/fly`, whose permission is `fuji.permission.fly`.
        But you don't want the players to use `/fly others <player>`, whose permission is `fuji.permission.fly.others.others`.
        In this case, you just enable the `permission implicitly inheritance` feature, and grant the `root permission`, then use rules to `exclude` special cases.

        NOTE: <red>Pre-defined rules only applied to non-operator players.</red>
        """)
    public List<CommandPermissionRule> rules = new ArrayList<>() {
        {
            this.add(new CommandPermissionRule("fuji.permission.*others.*", CommandPermissionTestResult.DENY_TO_USE_THE_COMMAND));
        }
    };

}

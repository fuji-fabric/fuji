package mod.fuji.core.command.config.model;

import mod.fuji.core.document.annotation.Document;
import java.util.TreeMap;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(id = 1754983291877L, value = """
    ◉ What is the permission of a command from this mod?
    1. Each command is registered with a default `level permission`.
    1.a. Check the value of `all_commands_require_level_4_permission_to_use_by_default` option in `config/fuji/config.json` file.
    1.a.i. If `true`, then `all` commands from this mod are registered with `level permission 4`.
    1.a.ii. If `false`, then `each` command from this mod is registered with `level permission N` defined in `config/fuji/permission.json` file.

    ◉ What is the `permission.json` file?
    This file is used to define the `default required level permission` for each command from this mod.
    After modifying this file, issue the `/reload` command to reload all the commands.

    This config is usually used in a `single-player` world, where `LuckPerms` mod is NOT installed.
    If you are hosting a dedicated Minecraft server, you may want to use the `command_permission` module.

    ◉ What if I want to use a `string permission` from `LuckPerms` mod?
    You can use `command_permission` module.
    That module is used to assign a `string permission` for each command, and `override` the original (the default level permission) requirement.
    In other words, the `default required level permission` defined in `permission.json` file will be `overridden` by `command_permission` module.
    """)
@Data
@NoArgsConstructor
public class PermissionModel {

    DefaultLevelPermission defaultLevelPermission = new DefaultLevelPermission();

    @Data
    @NoArgsConstructor
    public static class DefaultLevelPermission {
        TreeMap<String, Integer> commands = new TreeMap<>();
    }

}

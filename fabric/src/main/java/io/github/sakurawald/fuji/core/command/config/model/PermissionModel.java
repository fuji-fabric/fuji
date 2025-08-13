package io.github.sakurawald.fuji.core.command.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(id = 1754983291877L, value = """
    ◉ How is `permission` handled for each fuji command?
    1. Each fuji command is registered with a `default level permission`.
    1.a. Check the status of `all_commands_require_level_4_permission_to_use_by_default` option in `config.json` file.
    1.a.i. If it's `true`, then all fuji commands are registered with `level permission 4`.
    1.a.ii. If it's `false`, then each fuji command is registered with `level permission N` defined in `permission.json` file.
    2. You can use `command_permission` module to assign a `string permission` for each command, and override the `default level permission`.

    ◉ What is the `permission.json` file?
    This file is used to define the `default required level permission` for each registered fuji commands.
    After modifying this file, issue the `/reload` command to reload all the commands.

    This config is typically used in a `single-player world`, where no `LuckPerms` mod is installed.
    If you are hosting a dedicated Minecraft server, you can configure the `string permission` for each command using `command_permission` module.
    The `command_permission` module will override the `command requirement` defined in this file.
    So, if you are hosting a dedicated Minecraft server, you can simply use `command_permission` module, and ignore this file.
    """)
@Data
@NoArgsConstructor
public class PermissionModel {

    DefaultLevelPermission defaultLevelPermission = new DefaultLevelPermission();

    @Data
    @NoArgsConstructor
    public static class DefaultLevelPermission {
        Map<String, Integer> commands = new TreeMap<>();
    }

}

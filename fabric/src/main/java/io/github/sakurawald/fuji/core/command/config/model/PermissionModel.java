package io.github.sakurawald.fuji.core.command.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(id = 1754983291877L, value = """
    This file is used to define the `default required level permission` for each registered fuji commands.
    After you modify this file, issue `/reload` command to reload all the commands.
    <green>NOTE: If the client-side command tree didn't get updated after `/reload` command, you may need to `re-join` the server to refresh it.

    This config is typically used in `single-player world`, where there is no `LuckPerms` mod installed.
    If you are hosting a dedicated Minecraft server, you can configure the `string permission` for each command using `command_permission` module.
    The `command_permission` module will override the `command requirement` defined in this file.
    So, if you are hosting a dedicated Minecraft server, you can use `command_permission` module, and ignore this file.
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

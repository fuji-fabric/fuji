package io.github.sakurawald.fuji.core.command.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PermissionModel {

    @Document(id = 1754983291877L, value = """
        Define the `default required level permission` for each registered fuji commands.
        After you modify this file, issue `/reload` command to reload all the commands.

        <green>NOTE: This config is typically used in `single-player world`, where there is no `LuckPerms` mod installed.
        <green>NOTE: If you are hosting a dedicated Minecraft server, you can configure the `string permission` for each command using `command_permission` module.
        """)
    DefaultLevelPermission defaultLevelPermission = new DefaultLevelPermission();

    @Data
    @NoArgsConstructor
    public static class DefaultLevelPermission {
        Map<String, Integer> commands = new TreeMap<>();
    }

}

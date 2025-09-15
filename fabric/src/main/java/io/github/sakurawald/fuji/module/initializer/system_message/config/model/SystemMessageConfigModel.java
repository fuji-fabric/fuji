package io.github.sakurawald.fuji.module.initializer.system_message.config.model;


import io.github.sakurawald.fuji.module.initializer.system_message.structure.SystemMessageRule;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SystemMessageConfigModel {

    List<SystemMessageRule> rules = new ArrayList<>() {
        {
            this.add(new SystemMessageRule(true, "Modify the style of `/seed` command feedback.", "commands.seed.success", "<rainbow>Seeeeeeeeeeed: %s"));
        }
    };

}

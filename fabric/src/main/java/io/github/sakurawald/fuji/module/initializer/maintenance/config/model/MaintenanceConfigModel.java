package io.github.sakurawald.fuji.module.initializer.maintenance.config.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MaintenanceConfigModel {

    boolean maintenanceModeStatus = false;

    List<String> maintenanceMessages = new ArrayList<>() {
        {
            this.add("<gold>Maintenance in progress, please wait...");
            this.add("<blue>Currently under maintenance, check back soon!");
            this.add("<aqua>We’ll be back shortly after maintenance.");
            this.add("<light_purple>Maintenance ongoing, thanks for your patience!");
            this.add("<green>Server is in maintenance mode, hang tight!");
        }
    };

    Events events = new Events();

    @Data
    @NoArgsConstructor
    public static class Events {
        List<String> onEnterMaintenanceModeCommands = new ArrayList<>() {
            {
                this.add("send-broadcast <yellow><bold>Maintenance mode is now on.");
            }
        };
        List<String> onLeaveMaintenanceModeCommands = new ArrayList<>() {
            {
                this.add("send-broadcast <green><bold>Maintenance mode is now off.");
            }
        };
    }

}

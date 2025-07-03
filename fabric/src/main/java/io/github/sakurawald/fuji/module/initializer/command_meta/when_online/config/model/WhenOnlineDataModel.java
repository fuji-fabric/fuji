package io.github.sakurawald.fuji.module.initializer.command_meta.when_online.config.model;

import io.github.sakurawald.fuji.module.initializer.command_meta.when_online.structure.WhenOnlineTicket;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class WhenOnlineDataModel {
    public List<WhenOnlineTicket> tickets = new ArrayList<>();
}

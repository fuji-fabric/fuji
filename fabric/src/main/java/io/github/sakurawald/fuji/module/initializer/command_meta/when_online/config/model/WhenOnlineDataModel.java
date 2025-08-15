package io.github.sakurawald.fuji.module.initializer.command_meta.when_online.config.model;

import io.github.sakurawald.fuji.module.initializer.command_meta.when_online.structure.WhenOnlineTicket;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WhenOnlineDataModel {
    public CopyOnWriteArrayList<WhenOnlineTicket> tickets = new CopyOnWriteArrayList<>();
}

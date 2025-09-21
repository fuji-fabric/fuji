package mod.fuji.core.event.message.server;

import com.mojang.authlib.GameProfile;
import mod.fuji.core.event.message.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BypassPlayerLimitEvent extends BaseEvent {
    @NotNull GameProfile gameProfile;
    boolean canBypass;
}

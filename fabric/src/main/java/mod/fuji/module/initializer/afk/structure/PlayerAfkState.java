package mod.fuji.module.initializer.afk.structure;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlayerAfkState {
    boolean isAfk = false;
    long previousInputCounter = -1L;

}

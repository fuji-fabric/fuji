package io.github.sakurawald.fuji.module.initializer.system_message.structure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemMessageRule {

    boolean enable = true;
    @Nullable String document;
    boolean isScreenText = false;
    @NotNull String translatableTextKey;
    @Nullable String translatableTextValue;

}

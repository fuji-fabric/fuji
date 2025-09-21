package mod.fuji.core.config.mapper.wrapper;

import com.mojang.authlib.properties.Property;
import mod.fuji.core.auxiliary.minecraft.AuthlibHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyWrapper {
    String name;
    String value;
    String signature;

    public static @NotNull PropertyWrapper fromVanillaType(@NotNull Property property) {
        String name = AuthlibHelper.getPropertyName(property);
        String value = AuthlibHelper.getPropertyValue(property);
        String signature = AuthlibHelper.getPropertySignature(property);
        return new PropertyWrapper(name, value, signature);
    }

    public @NotNull Property toVanillaType() {
        return new Property(name, value, signature);
    }

}

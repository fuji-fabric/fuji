package mod.fuji.core.config.mapper.representation;

import com.mojang.authlib.properties.Property;
import mod.fuji.core.auxiliary.minecraft.AuthlibHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyIR {
    String name;
    String value;
    String signature;

    public static @NotNull PropertyIR fromNative(@NotNull Property property) {
        String name = AuthlibHelper.getPropertyName(property);
        String value = AuthlibHelper.getPropertyValue(property);
        String signature = AuthlibHelper.getPropertySignature(property);
        return new PropertyIR(name, value, signature);
    }

    public @NotNull Property toNative() {
        return new Property(this.name, this.value, this.signature);
    }

}

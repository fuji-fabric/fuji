package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import com.mojang.authlib.properties.Property;
import org.jetbrains.annotations.NotNull;

public class AuthlibHelper {

    public static String getPropertyName(@NotNull Property property) {
        #if MC_VER <= MC_1_20_1
        return property.getName();
        #elif MC_VER > MC_1_20_1
        return property.name();
        #endif
    }

    public static String getPropertyValue(@NotNull Property property) {
        #if MC_VER <= MC_1_20_1
        return property.getValue();
        #elif MC_VER > MC_1_20_1
        return property.value();
        #endif
    }

    public static String getPropertySignature(@NotNull Property property) {
        #if MC_VER <= MC_1_20_1
        return property.getSignature();
        #elif MC_VER > MC_1_20_1
        return property.signature();
        #endif
    }
}

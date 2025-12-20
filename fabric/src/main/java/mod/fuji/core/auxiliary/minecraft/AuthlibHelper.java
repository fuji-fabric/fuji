package mod.fuji.core.auxiliary.minecraft;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import mod.fuji.core.auxiliary.LogUtil;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public class AuthlibHelper {

    public static final String TEXTURES_PROPERTY_KEY = "textures";

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

    public static @NotNull String getGameProfileName(@NotNull GameProfile gameProfile) {
        #if MC_VER < MC_1_21_9
        return gameProfile.getName();
        #elif MC_VER >= MC_1_21_9
        return gameProfile.name();
        #endif
    }

    public static @NotNull UUID getGameProfileId(@NotNull GameProfile gameProfile) {
        #if MC_VER < MC_1_21_9
        return gameProfile.getId();
        #elif MC_VER >= MC_1_21_9
        return gameProfile.id();
        #endif
    }

    public static @NotNull PropertyMap getGameProfileProperties(@NotNull GameProfile gameProfile) {
        #if MC_VER < MC_1_21_9
        return gameProfile.getProperties();
        #elif MC_VER >= MC_1_21_9
        return gameProfile.properties();
        #endif
    }

    public static @NotNull PropertyMap makePropertyMap() {
        return makePropertyMap(LinkedHashMultimap.create());
    }

    public static @NotNull PropertyMap makePropertyMap(@NotNull Multimap<String, Property> properties) {
        #if MC_VER < MC_1_21_9
        PropertyMap propertyMap =  new PropertyMap();
        propertyMap.putAll(properties);
        return propertyMap;
        #elif MC_VER >= MC_1_21_9
        return new PropertyMap(properties);
        #endif
    }

    public static void modifyGameProfile(@NotNull GameProfile gameProfile, @NotNull Property textureProperty) {
        String name = AuthlibHelper.getGameProfileName(gameProfile);
        LogUtil.debug("Modify game profile: name = {}, texture property = {}", name, textureProperty);

        PropertyMap properties = AuthlibHelper.getGameProfileProperties(gameProfile);
        properties.removeAll(TEXTURES_PROPERTY_KEY);
        properties.put(TEXTURES_PROPERTY_KEY, textureProperty);
    }

}

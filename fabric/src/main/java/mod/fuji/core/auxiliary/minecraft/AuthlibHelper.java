package mod.fuji.core.auxiliary.minecraft;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.config.mapper.wrapper.GameProfileWrapper;
import java.util.UUID;
import net.minecraft.util.UserCache;
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

    public static void modifyGameProfile(@NotNull GameProfile gameProfile, @NotNull Property skin) {
        String name = AuthlibHelper.getName(gameProfile);
        LogUtil.debug("Modify the skin property for player {}. (skin = {})", name, skin);

        PropertyMap properties = AuthlibHelper.getProperties(gameProfile);
        properties.removeAll("textures");
        properties.put("textures", skin);
    }

    public static @NotNull String getName(@NotNull GameProfile gameProfile) {
        #if MC_VER < MC_1_21_9
        return gameProfile.getName();
        #elif MC_VER >= MC_1_21_9
        return gameProfile.name();
        #endif
    }

    public static @NotNull UUID getId(@NotNull GameProfile gameProfile) {
        #if MC_VER < MC_1_21_9
        return gameProfile.getId();
        #elif MC_VER >= MC_1_21_9
        return gameProfile.id();
        #endif
    }

    public static @NotNull PropertyMap getProperties(@NotNull GameProfile gameProfile) {
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

    public static @NotNull GameProfile getGameProfile(@NotNull UserCache.Entry entry) {
        #if MC_VER < MC_1_21_9
        return entry.getProfile();
        #elif MC_VER >= MC_1_21_9
        return GameProfileWrapper.toGameProfile(entry.getPlayer());
        #endif
    }


}

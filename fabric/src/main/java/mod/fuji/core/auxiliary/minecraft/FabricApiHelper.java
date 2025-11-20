package mod.fuji.core.auxiliary.minecraft;

import mod.fuji.core.auxiliary.LogUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

public class FabricApiHelper {

    private static final String FABRIC_API_EVENT_INVOKER_METHOD_NAME = "invoker";
    private static final String SERVER_WORLD_EVENTS_CLASS_NAME = "net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents";

    private static boolean isFabricApiModInstalled() {
        return FabricLoader.getInstance().isModLoaded("fabric-api");
    }

    public static void fireOnWorldLoadEvent(@NotNull MinecraftServer server, @NotNull ServerLevel serverWorld) throws Exception {
        List<Class<?>> eventParameters = List.of(MinecraftServer.class, ServerLevel.class);
        List<Object> eventArguments = List.of(server, serverWorld);
        FabricApiHelper.fireEvent(FabricApiHelper.SERVER_WORLD_EVENTS_CLASS_NAME, "LOAD", "onWorldLoad", eventParameters, eventArguments);
    }

    public static void fireOnWorldUnloadEvent(@NotNull MinecraftServer server, @NotNull ServerLevel serverWorld) throws Exception {
        List<Class<?>> eventParameters = List.of(MinecraftServer.class, ServerLevel.class);
        List<Object> eventArguments = List.of(server, serverWorld);
        FabricApiHelper.fireEvent(FabricApiHelper.SERVER_WORLD_EVENTS_CLASS_NAME, "UNLOAD", "onWorldUnload", eventParameters, eventArguments);
    }

    @SuppressWarnings("SameParameterValue")
    private static void fireEvent(@NotNull String eventDeclaringClassName, @NotNull String eventDeclaringFieldName, @NotNull String eventFireMethodName, @NotNull List<Class<?>> eventFireMethodParameterList, @NotNull List<Object> eventFireMethodArgumentList) throws Exception {
        LogUtil.debug("fireEvent(): try firing an event. (eventDeclaringClassName = {}, eventDeclaringFieldName = {}, eventFireMethodName = {}, eventFireMethodParameterList = {}, eventFireMethodArgumentList = {})", eventDeclaringClassName, eventDeclaringFieldName, eventFireMethodName, eventFireMethodParameterList, eventFireMethodArgumentList);

        if (!isFabricApiModInstalled()) {
            LogUtil.debug("fireEvent(): The `fabric-api` mod is not installed, skip the firing of the event.");
            return;
        }

        /* Get the event declaring class. */
        Class<?> eventDeclaringClass = Class.forName(eventDeclaringClassName);
        LogUtil.debug("fireEvent(): Class<?> eventDeclaringClass = {}", eventDeclaringClass);

        /* Get the event field. */
        Field eventDeclaringField = eventDeclaringClass.getDeclaredField(eventDeclaringFieldName);
        LogUtil.debug("fireEvent(): Field eventDeclaringField = {}", eventDeclaringField);

        /* Get the event class. */
        eventDeclaringField.setAccessible(true);
        Object eventInstance = eventDeclaringField.get(null);
        Class<?> eventClass = eventInstance.getClass();
        LogUtil.debug("fireEvent(): Object eventInstance = {}", eventInstance);

        /* Get the invoker() method and call it. */
        Method invokerMethod = eventClass.getMethod(FABRIC_API_EVENT_INVOKER_METHOD_NAME);
        LogUtil.debug("fireEvent(): Method invokerMethod = {}", invokerMethod);

        Object eventInvokerInstance = invokerMethod.invoke(eventInstance);
        Class<?> eventInvokerClass = eventInvokerInstance.getClass();
        LogUtil.debug("fireEvent(): Object eventInvokerInstance = {}", eventInvokerInstance);

        /* Get the event fire method. */
        Class<?>[] eventFireMethodParameterArray = eventFireMethodParameterList.toArray(new Class[0]);
        Method eventFireMethod = eventInvokerClass.getMethod(eventFireMethodName, eventFireMethodParameterArray);

        /* Invoke the fire method. */
        Object[] eventFireMethodArgumentArray = eventFireMethodArgumentList.toArray(new Object[0]);
        eventFireMethod.setAccessible(true);
        eventFireMethod.invoke(eventInvokerInstance, eventFireMethodArgumentArray);
    }
}

package mod.fuji.core.service.command_callback;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.annotation.Unused;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.RandomUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.command.CommandRegistrationEvent;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.core.service.command_callback.structure.TTLMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.ClickEvent;
import org.jetbrains.annotations.NotNull;

public class CommandCallbackManager {

    private static final String COMMAND_CALLBACK_LITERAL = "command-callback";
    private static final String COMMAND_CALLBACK_UUID_ARGUMENT_NAME = "uuid";

    private static TTLMap<String, Consumer<ServerPlayer>> uuid2consumer;

    @EventConsumer
    private static void resetCallbackMap(@Unused ServerStartedEvent event) {
        uuid2consumer = new TTLMap<>();
    }

    @EventConsumer
    private static void registerCommandCallbackCommand(CommandRegistrationEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher
            .register(
                literal(COMMAND_CALLBACK_LITERAL)
                    .then(argument(COMMAND_CALLBACK_UUID_ARGUMENT_NAME, StringArgumentType.greedyString())
                        .executes(CommandCallbackManager::$commandCallback)));
    }

    private static int $commandCallback(@NotNull CommandContext<CommandSourceStack> ctx) {
        return CommandHelper.Pattern.withServerPlayerCommand(ctx.getSource(), player -> {
            String uuid = StringArgumentType.getString(ctx, COMMAND_CALLBACK_UUID_ARGUMENT_NAME);
            invokeCallbackWithPlayer(uuid, player);
            return CommandHelper.Return.SUCCESS;
        });
    }

    private static void invokeCallbackWithPlayer(@NotNull String uuid, @NotNull ServerPlayer player) {
        Consumer<ServerPlayer> consumer = uuid2consumer.get(uuid);
        if (consumer == null) {
            TextHelper.sendTextByKey(player, "callback.invalid");
            return;
        }

        consumer.accept(player);
    }

    private static @NotNull String makeCallbackCommandString(@NotNull String uuid, @NotNull Consumer<ServerPlayer> callback, long ttl, @NotNull TimeUnit timeUnit) {
        LogUtil.debug("Make callback command: uuid = {}", uuid);
        uuid2consumer.put(uuid, callback, ttl, timeUnit);
        return "/" + COMMAND_CALLBACK_LITERAL + " " + uuid;
    }

    public static @NotNull String makeCallbackCommandString(@NotNull Consumer<ServerPlayer> callback, long ttl, @NotNull TimeUnit timeUnit) {
        return CommandCallbackManager.makeCallbackCommandString(RandomUtil.randomUUID(), callback, ttl, timeUnit);
    }

    private static @NotNull ClickEvent makeCallbackClickEvent(@NotNull String uuid, @NotNull Consumer<ServerPlayer> callback, long ttl, @NotNull TimeUnit timeUnit) {
        String commandString = makeCallbackCommandString(uuid, callback, ttl, timeUnit);
        return TextHelper.Events
            .ClickEvent
            .makeRunCommandAction(commandString);
    }

    public static @NotNull ClickEvent makeCallbackClickEvent(@NotNull Consumer<ServerPlayer> callback, long ttl, @NotNull TimeUnit timeUnit) {
        return makeCallbackClickEvent(RandomUtil.randomUUID(), callback, ttl, timeUnit);
    }

}

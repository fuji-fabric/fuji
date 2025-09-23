package mod.fuji.core.manager.impl.callback;

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
import mod.fuji.core.manager.abst.BaseManager;
import mod.fuji.core.manager.impl.callback.structure.TTLMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;

public class CallbackManager extends BaseManager {
    private static final String COMMAND_CALLBACK_LITERAL = "command-callback";
    private static final String COMMAND_CALLBACK_UUID_ARGUMENT_NAME = "uuid";

    private static TTLMap<String, Consumer<ServerPlayerEntity>> uuid2consumer;

    @Override
    public void onInitialize() {}

    @EventConsumer
    private static void resetCallbackMap(@Unused ServerStartedEvent event) {
        uuid2consumer = new TTLMap<>();
    }

    @EventConsumer
    private static void registerCommandCallbackCommand(CommandRegistrationEvent event) {
        CommandDispatcher<ServerCommandSource> dispatcher = event.getDispatcher();
        dispatcher
            .register(
                literal(COMMAND_CALLBACK_LITERAL)
                    .then(argument(COMMAND_CALLBACK_UUID_ARGUMENT_NAME, StringArgumentType.greedyString())
                        .executes(CallbackManager::$executeCallbackCommand)));
    }

    private static int $executeCallbackCommand(CommandContext<ServerCommandSource> ctx) {
        return CommandHelper.Pattern.withServerPlayerCommand(ctx.getSource(), player -> {
            String uuid = StringArgumentType.getString(ctx, COMMAND_CALLBACK_UUID_ARGUMENT_NAME);
            executeCallbackCommand(uuid, player);
            return CommandHelper.Return.SUCCESS;
        });
    }

    private static void executeCallbackCommand(String uuid, ServerPlayerEntity player) {
        Consumer<ServerPlayerEntity> consumer = uuid2consumer.get(uuid);
        if (consumer == null) {
            TextHelper.sendTextByKey(player, "callback.invalid");
            return;
        }

        consumer.accept(player);
    }

    private String makeCallbackCommand(String uuid, Consumer<ServerPlayerEntity> callback, long ttl, TimeUnit timeUnit) {
        LogUtil.debug("Make callback command: uuid = {}", uuid);
        uuid2consumer.put(uuid, callback, ttl, timeUnit);
        return "/" + COMMAND_CALLBACK_LITERAL + " " + uuid;
    }

    public String makeCallbackCommand(Consumer<ServerPlayerEntity> callback, long ttl, TimeUnit timeUnit) {
        return this.makeCallbackCommand(RandomUtil.randomUUID(), callback, ttl, timeUnit);
    }

    private ClickEvent makeCallbackEvent(String uuid, Consumer<ServerPlayerEntity> callback, long ttl, TimeUnit timeUnit) {
        String commandString = makeCallbackCommand(uuid, callback, ttl, timeUnit);
        return TextHelper.Events
            .ClickEvent
            .makeRunCommandAction(commandString);
    }

    public ClickEvent makeCallbackEvent(Consumer<ServerPlayerEntity> callback, long ttl, TimeUnit timeUnit) {
        return this.makeCallbackEvent(RandomUtil.randomUUID(), callback, ttl, timeUnit);
    }
}

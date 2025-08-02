package io.github.sakurawald.fuji.core.manager.impl.callback;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.RandomUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.event.impl.CommandEvents;
import io.github.sakurawald.fuji.core.manager.abst.BaseManager;
import io.github.sakurawald.fuji.core.manager.impl.callback.structure.TTLMap;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CallbackManager extends BaseManager {
    private static final String COMMAND_CALLBACK_LITERAL = "command-callback";
    private static final String COMMAND_CALLBACK_UUID_ARGUMENT_NAME = "uuid";

    private final TTLMap<String, Consumer<ServerPlayerEntity>> uuid2consumer = new TTLMap<>();

    @Override
    public void onInitialize() {
        this.registerUserCommand();
    }

    private void registerUserCommand() {
        CommandEvents.REGISTRATION.register((dispatcher, registryAccess, environment) -> dispatcher.register(
            literal(COMMAND_CALLBACK_LITERAL)
                .then(argument(COMMAND_CALLBACK_UUID_ARGUMENT_NAME, StringArgumentType.greedyString())
                    .executes(this::$executeCallbackCommand))));
    }

    private int $executeCallbackCommand(CommandContext<ServerCommandSource> ctx) {
        return CommandHelper.Pattern.playerOnlyCommand(ctx.getSource(), player -> {
            String uuid = StringArgumentType.getString(ctx, COMMAND_CALLBACK_UUID_ARGUMENT_NAME);
            this.executeCallbackCommand(uuid, player);
            return CommandHelper.Return.SUCCESS;
        });
    }

    private void executeCallbackCommand(String uuid, ServerPlayerEntity player) {
        Consumer<ServerPlayerEntity> consumer = this.uuid2consumer.get(uuid);
        if (consumer == null) {
            TextHelper.sendTextByKey(player, "callback.invalid");
            return;
        }

        consumer.accept(player);
    }

    private String makeCallbackCommand(String uuid, Consumer<ServerPlayerEntity> callback, long ttl, TimeUnit timeUnit) {
        LogUtil.debug("Make callback command: uuid = {}", uuid);
        this.uuid2consumer.put(uuid, callback, ttl, timeUnit);
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

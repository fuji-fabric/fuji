package io.github.sakurawald.fuji.module.initializer.tester;


import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.SneakyThrows;
import net.minecraft.command.ReturnValueConsumer;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.thread.FutureQueue;

@Document(id = 1751980891153L, value = """
    This module is only used for `development`.
    If you are a developer, you can try new things here.
    You don't need to enable this module in production environment.
    It does not harm, but also not useful.
    """)
@CommandNode("tester")
@CommandRequirement(level = 4)
public class TesterInitializer extends ModuleInitializer {

    @SneakyThrows(Exception.class)
    @CommandNode("run")
    private static int $run(@CommandSource ServerCommandSource source, GreedyString commandLine) {
        callSmartUsage(source,commandLine);

        LogUtil.info("Done");
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("greeter")
    private static int $greeter(@CommandSource ServerCommandSource source) {
        FeatureSet enabledFeatures = source.getEnabledFeatures();
        LogUtil.info("enabled features = {}", enabledFeatures);

        Collection<String> chatSuggestions = source.getChatSuggestions();
        LogUtil.info("chat suggestions = {}", chatSuggestions);

        ReturnValueConsumer returnValueConsumer = source.getReturnValueConsumer();
        LogUtil.info("returnValueConsumer = {}", returnValueConsumer);

        FutureQueue messageChainTaskQueue = source.getMessageChainTaskQueue();
        messageChainTaskQueue.append(() -> {
            LogUtil.info("chain task: 1");
        });
        messageChainTaskQueue.append(() -> {
            LogUtil.info("chain task: 2");
        });
        CompletableFuture<Integer> integerCompletableFuture = CompletableFuture.supplyAsync(() -> {
//            while (true) {}
            return 42;
        });

        boolean silent = source.isSilent();
        LogUtil.info("silent = {}", silent);

        messageChainTaskQueue.append(integerCompletableFuture, (intValue) -> {
            LogUtil.info("value is: {}", intValue);
        });
        LogUtil.info("messageChainTaskQueue = {}", messageChainTaskQueue);

        MutableText text = Text.literal("Hello %s".formatted(source.getName()));
        source.sendMessage(text);
        return CommandHelper.Return.SUCCESS;
    }

    private static void callAllUsage(ServerCommandSource source, GreedyString commandLine) {
        CommandDispatcher<ServerCommandSource> commandDispatcher = CommandHelper.getCommandDispatcher();
        ParseResults<ServerCommandSource> parseResults = commandDispatcher.parse(commandLine.getValue(), source);
        parseResults.getExceptions().forEach((k, v) -> {
            source.sendMessage(Text.literal("k = %s v = %s".formatted(k, v)));
        });

        String[] map = commandDispatcher.getAllUsage(Iterables.getLast(parseResults.getContext().getNodes()).getNode(), source, true);
        for (String string : map) {
            source.sendMessage(Text.literal("/" + parseResults.getReader().getString() + " " + string));
        }

    }

    private static void callSmartUsage(ServerCommandSource source, GreedyString commandLine) {
        CommandDispatcher<ServerCommandSource> commandDispatcher = CommandHelper.getCommandDispatcher();
        ParseResults<ServerCommandSource> parseResults = commandDispatcher.parse(commandLine.getValue(), source);
        parseResults.getExceptions().forEach((k, v) -> {
            source.sendMessage(Text.literal("k = %s v = %s".formatted(k, v)));
        });

        Map<com.mojang.brigadier.tree.CommandNode<ServerCommandSource>, String> map = commandDispatcher.getSmartUsage(Iterables.getLast(parseResults.getContext().getNodes()).getNode(), source);
        for (String string : map.values()) {
            source.sendMessage(Text.literal("/" + parseResults.getReader().getString() + " " + string));
        }

    }

    @CommandNode("split")
    private static int $split(@CommandSource ServerCommandSource source, @CommandTarget ServerPlayerEntity target, String string) {
        TextHelper.sendBroadcastByText(Text.literal("Run split(): source = %s, target = %s, string = %s".formatted(source.getName(), PlayerHelper.getPlayerName(target), string)));
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("non-split")
    private static int $nonSplit(@CommandSource @CommandTarget ServerPlayerEntity source, String string) {
        TextHelper.sendBroadcastByText(Text.literal("Run non-split(): source = %s, string = %s".formatted(PlayerHelper.getPlayerName(source), string)));
        return CommandHelper.Return.SUCCESS;
    }

}

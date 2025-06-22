package io.github.sakurawald.module.initializer.placeholder;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import io.github.sakurawald.Fuji;
import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.ChronosUtil;
import io.github.sakurawald.core.auxiliary.RandomUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.PermissionHelper;
import io.github.sakurawald.core.auxiliary.minecraft.PlaceholderHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.placeholder.gui.PlaceholderGui;
import io.github.sakurawald.module.initializer.placeholder.job.UpdateSumUpPlaceholderJob;
import io.github.sakurawald.module.initializer.placeholder.structure.SumUpPlaceholder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Document("""
    Provides extra placeholders for `Text Placeholder API` mod.
    """)
@CommandNode("placeholder")
@CommandRequirement(level = 4)
public class PlaceholderInitializer extends ModuleInitializer {

    private static final Map<String, Map<String, String>> ROTATE_CACHE = new HashMap<>();

    private static final Pattern ESCAPE_PARSER = Pattern.compile("\\s*([\\s\\S]+)\\s+(\\d+)\\s*");

    @CommandNode("list")
    @Document("List all placeholders registered in server.")
    private static int list(@CommandSource ServerPlayerEntity player) {
        List<Identifier> list = Placeholders.getPlaceholders().keySet().asList();
        new PlaceholderGui(player, list, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("parse")
    @Document("Parse a placeholder with a contextual player.")
    private static int list(@CommandSource ServerCommandSource source
        , Optional<ServerPlayerEntity> player
        , GreedyString input) {
        ServerPlayerEntity target = player.orElse(null);

        Text text = TextHelper.getTextByValue(target, input.getValue());
        source.sendMessage(text);
        return CommandHelper.Return.SUCCESS;
    }

    private static void registerServerPlaytimePlaceholder() {
        PlaceholderHelper.withServer("server_playtime", server -> Text.literal(String.valueOf(SumUpPlaceholder.ofServer().playtime)));
    }

    private static void registerPlayerPlaytimePlaceholder() {
        PlaceholderHelper.withPlayer("player_playtime", player -> Text.literal(String.valueOf(SumUpPlaceholder.ofPlayer(player.getUuidAsString()).playtime)));
    }

    private static void registerServerMovedPlaceholder() {
        PlaceholderHelper.withServer("server_moved", server -> Text.literal(String.valueOf(SumUpPlaceholder.ofServer().moved)));
    }

    private static void registerPlayerMovedPlaceholder() {
        PlaceholderHelper.withPlayer("player_moved", player -> Text.literal(String.valueOf(SumUpPlaceholder.ofPlayer(player.getUuidAsString()).moved)));
    }

    private static void registerServerKilledPlaceholder() {
        PlaceholderHelper.withServer("server_killed", server -> Text.literal(String.valueOf(SumUpPlaceholder.ofServer().killed)));
    }

    private static void registerPlayerKilledPlaceholder() {
        PlaceholderHelper.withPlayer("player_killed", player -> Text.literal(String.valueOf(SumUpPlaceholder.ofPlayer(player.getUuidAsString()).killed)));
    }

    private static void registerServerPlacedPlaceholder() {
        PlaceholderHelper.withServer("server_placed", server -> Text.literal(String.valueOf(SumUpPlaceholder.ofServer().placed)));
    }

    private static void registerPlayerPlacedPlaceholder() {
        PlaceholderHelper.withPlayer("player_placed", player -> Text.literal(String.valueOf(SumUpPlaceholder.ofPlayer(player.getUuidAsString()).placed)));
    }

    private static void registerServerMinedPlaceholder() {
        PlaceholderHelper.withServer("server_mined", (server) -> Text.literal(String.valueOf(SumUpPlaceholder.ofServer().mined)));
    }

    private static void registerPlayerMinedPlaceholder() {
        PlaceholderHelper.withPlayer("player_mined", player -> Text.literal(String.valueOf(SumUpPlaceholder.ofPlayer(player.getUuidAsString()).mined)));
    }

    public static void registerPrefixPlaceholder() {
        PlaceholderHelper.withPlayer("player_prefix", (player, arg) -> {
            String prefix = PermissionHelper.getPrefix(player.getUuid());
            return TextHelper.getTextByValue(player, prefix);
        });
    }

    public static void registerSuffixPlaceholder() {
        PlaceholderHelper.withPlayer("player_suffix", (player, arg) -> {
            String prefix = PermissionHelper.getSuffix(player.getUuid());
            return TextHelper.getTextByValue(player, prefix);
        });
    }

    public static void registerPosPlaceholder() {
        PlaceholderHelper.withPlayer("pos", (player) -> {
            int x = player.getBlockX();
            int y = player.getBlockY();
            int z = player.getBlockZ();
            String dim_name = player.getWorld().getRegistryKey().getValue().toString();
            String dim_display_name = TextHelper.getValueByKey(player, dim_name);
            String hoverString = TextHelper.getValueByKey(player, "chat.current_pos");
            switch (dim_name) {
                case "minecraft:overworld":
                    hoverString += "\n" + TextHelper.getValueByKey(player, "minecraft:the_nether")
                        + ": %d %s %d".formatted(x / 8, y, z / 8);
                    break;
                case "minecraft:the_nether":
                    hoverString += "\n" + TextHelper.getValueByKey(player, "minecraft:overworld")
                        + ": %d %s %d".formatted(x * 8, y, z * 8);
                    break;
            }

            String clickCommand = TextHelper.getValueByKey(player, "chat.xaero_waypoint_add.command");

            return TextHelper.getTextByKey(player, "placeholder.pos", x, y, z, dim_display_name)
                .copy()
                .fillStyle(Style.EMPTY
                    .withHoverEvent(TextHelper.HoverEvent.makeShowTextAction(Text.literal(hoverString + "\n")
                            .append(TextHelper.getTextByKey(player, "chat.xaero_waypoint_add"))
                    ))
                    .withClickEvent(TextHelper.ClickEvent.makeRunCommandAction(clickCommand))
                );
        });
    }

    @Override
    protected void onInitialize() {
        /* register placeholders */
        registerPlayerMinedPlaceholder();
        registerServerMinedPlaceholder();

        registerPlayerPlacedPlaceholder();
        registerServerPlacedPlaceholder();

        registerPlayerKilledPlaceholder();
        registerServerKilledPlaceholder();


        registerPlayerMovedPlaceholder();
        registerServerMovedPlaceholder();

        registerPlayerPlaytimePlaceholder();
        registerServerPlaytimePlaceholder();

        registerHealthBarPlaceholder();
        registerRotatePlaceholder();
        registerHasPermissionPlaceholder();
        registerGetMetaPlaceholder();
        registerRandomPlayerPlaceholder();
        registerRandomPlaceholder();
        registerEscapePlaceholder();
        registerProtectPlaceholder();
        registerDatePlaceholder();

        registerPrefixPlaceholder();
        registerSuffixPlaceholder();

        registerPosPlaceholder();

        /* events */
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            SumUpPlaceholder.ofServer();
            new UpdateSumUpPlaceholderJob().schedule();
        });
    }

    private void registerDatePlaceholder() {
        PlaceholderHelper.withServer("date", (server, arg) -> {
            if (arg == null || arg.isEmpty()) {
                return Text.literal(ChronosUtil.getCurrentDate());
            }

            try {
                String currentDate = ChronosUtil.getCurrentDate(new SimpleDateFormat(arg));
                return Text.literal(currentDate);
            } catch (Exception e) {
                return Text.of("Invalid date formatter: " + arg);
            }
        });
    }

    private void registerEscapePlaceholder() {
        PlaceholderHelper.withServer("escape", (server, args) -> {
            if (args == null) return PlaceholderHelper.INVALID_TEXT;

            Matcher matcher = ESCAPE_PARSER.matcher(args);
            if (matcher.find()) {
                String placeholder = matcher.group(1);
                int level = Integer.parseInt(matcher.group(2));

                if (level == 1) return Text.literal("%" + placeholder + "%");
                if (level > 1)
                    return Text.literal("%fuji:escape " + placeholder + " " + (level - 1) + "%");
            }
            return Text.literal("%" + args + "%");
        });
    }

    private void registerProtectPlaceholder() {
        PlaceholderHelper.withServer("protect", (server, args) -> {
            if (args == null) return Text.empty();
            return Text.literal(args);
        });
    }

    private void registerHasPermissionPlaceholder() {
        PlaceholderHelper.withPlayer("has_permission", (player, args) -> {
            boolean value = PermissionHelper.hasPermission(player.getUuid(), args);
            return Text.literal(String.valueOf(value));
        });
    }

    private void registerGetMetaPlaceholder() {
        PlaceholderHelper.withPlayer("get_meta", (player, args) -> {
            Optional<String> metaValue = PermissionHelper.getMeta(player.getUuid(), args, String::valueOf);
            return Text.literal(metaValue.orElse("META_NOT_FOUND"));
        });
    }

    private void registerRandomPlayerPlaceholder() {
        PlaceholderHelper.withServer("random_player", (server, args) -> {
            List<ServerPlayerEntity> playerList = ServerHelper.getPlayers();
            ServerPlayerEntity serverPlayerEntity = RandomUtil.drawList(playerList);
            return Text.literal(serverPlayerEntity.getGameProfile().getName());
        });
    }

    private void registerRandomPlaceholder() {
        PlaceholderHelper.withServer("random", (server, args) -> {
            if (args == null) return PlaceholderHelper.INVALID_TEXT;

            String[] split = args.split(" ");
            if (split.length != 2) return PlaceholderHelper.INVALID_TEXT;

            int i;
            try {
                i = RandomUtil.getRandom().nextInt(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            } catch (Exception e) {
                return PlaceholderHelper.INVALID_TEXT;
            }

            return Text.literal(String.valueOf(i));
        });
    }

    private void registerHealthBarPlaceholder() {
        PlaceholderHelper.withPlayer("health_bar", player -> {
            int totalHearts = 10;
            int filledHearts = (int) (player.getHealth() / 2);
            int unfilledHearts = totalHearts - filledHearts;
            String str = "♥".repeat(filledHearts) + "♡".repeat(unfilledHearts);
            return Text.literal(str);
        });
    }

    private void registerRotatePlaceholder() {
        Placeholders.register(Identifier.of(Fuji.MOD_ID, "rotate"), (ctx, args) -> {
            String namespace = "default";
            if (ctx.player() != null) {
                namespace = ctx.player().getGameProfile().getName();
            }

            ROTATE_CACHE.putIfAbsent(namespace, new HashMap<>());
            Map<String, String> rotateMap = ROTATE_CACHE.get(namespace);
            rotateMap.putIfAbsent(args, args);

            String frame = rotateMap.get(args);
            rotateMap.put(args, StringUtils.rotate(frame, -1));

            return PlaceholderResult.value(Text.literal(frame));
        });
    }

}

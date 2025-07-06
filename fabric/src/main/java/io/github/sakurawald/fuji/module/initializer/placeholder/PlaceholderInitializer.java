package io.github.sakurawald.fuji.module.initializer.placeholder;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.RandomUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlaceholderHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.event.impl.PlayerEvents;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.document.descriptor.MetaDescriptor;
import io.github.sakurawald.fuji.core.document.descriptor.PlaceholderDescriptor;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import io.github.sakurawald.fuji.module.initializer.placeholder.gui.PlaceholderGui;
import io.github.sakurawald.fuji.module.initializer.placeholder.job.UpdateSumUpPlaceholderJob;
import io.github.sakurawald.fuji.module.initializer.placeholder.structure.SumUpPlaceholder;
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

    @Document("List all placeholders registered in server.")
    @CommandNode("list")
    private static int list(@CommandSource ServerPlayerEntity player) {
        List<Identifier> list = Placeholders.getPlaceholders().keySet().asList();
        new PlaceholderGui(player, list, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Parse a placeholder with a contextual player.")
    @CommandNode("parse")
    private static int list(@CommandSource ServerCommandSource source
        , Optional<ServerPlayerEntity> player
        , GreedyString input) {
        ServerPlayerEntity target = player.orElse(null);

        Text text = TextHelper.getTextByValue(target, input.getValue());
        source.sendMessage(text);
        return CommandHelper.Return.SUCCESS;
    }

    private static void registerServerPlaytimePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("server_playtime", """
            Returns the total playtime of all players in the server.
            """);
        PlaceholderHelper.registerServerPlaceholder(descriptor, server -> Text.literal(String.valueOf(SumUpPlaceholder.ofServer().playtime)));
    }

    private static void registerPlayerPlaytimePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("player_playtime", """
            Returns the playtime of the player.
            """);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, player -> Text.literal(String.valueOf(SumUpPlaceholder.ofPlayer(player.getUuidAsString()).playtime)));
    }

    private static void registerServerMovedPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("server_moved", """
            Returns the total distance traveled by all players in the server.
            """);
        PlaceholderHelper.registerServerPlaceholder(descriptor, server -> Text.literal(String.valueOf(SumUpPlaceholder.ofServer().moved)));
    }

    private static void registerPlayerMovedPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("player_moved", """
            Returns the distance traveled by the player.
            """);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, player -> Text.literal(String.valueOf(SumUpPlaceholder.ofPlayer(player.getUuidAsString()).moved)));
    }

    private static void registerServerKilledPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("server_killed", """
            Returns the total entities killed by all players in the server.
            """);
        PlaceholderHelper.registerServerPlaceholder(descriptor, server -> Text.literal(String.valueOf(SumUpPlaceholder.ofServer().killed)));
    }

    private static void registerPlayerKilledPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("player_killed", """
            Returns the killed entities by the player.
            """);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, player -> Text.literal(String.valueOf(SumUpPlaceholder.ofPlayer(player.getUuidAsString()).killed)));
    }

    private static void registerServerPlacedPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("server_placed", """
            Returns the total placed blocks by all players in the server.
            """);
        PlaceholderHelper.registerServerPlaceholder(descriptor, server -> Text.literal(String.valueOf(SumUpPlaceholder.ofServer().placed)));
    }

    private static void registerPlayerPlacedPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("player_placed", """
            Returns the placed blocks by the player.
            """);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, player -> Text.literal(String.valueOf(SumUpPlaceholder.ofPlayer(player.getUuidAsString()).placed)));
    }

    private static void registerServerMinedPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("server_mined", """
            Returns the total mined blocks by all players in the server.
            """);
        PlaceholderHelper.registerServerPlaceholder(descriptor, (server) -> Text.literal(String.valueOf(SumUpPlaceholder.ofServer().mined)));
    }

    private static void registerPlayerMinedPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("player_mined", """
            Returns the mined blocks by the player.
            """);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, player -> Text.literal(String.valueOf(SumUpPlaceholder.ofPlayer(player.getUuidAsString()).mined)));
    }

    public static void registerPrefixPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("player_prefix", """
            Returns the `luckperms prefix` of the player.
            """);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player, arg) -> {
            String prefix = LuckpermsHelper.getPrefix(player.getUuid());
            return TextHelper.getTextByValue(player, prefix);
        });
    }

    public static void registerSuffixPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("player_suffix", """
            Returns the `luckperms suffix` of the player.
            """);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player, arg) -> {
            String prefix = LuckpermsHelper.getSuffix(player.getUuid());
            return TextHelper.getTextByValue(player, prefix);
        });
    }

    public static void registerPosPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("pos", """
            Returns the `location` of the player.
            """);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> {
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
                    .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(Text.literal(hoverString + "\n")
                            .append(TextHelper.getTextByKey(player, "chat.xaero_waypoint_add"))
                    ))
                    .withClickEvent(TextHelper.Events.ClickEvent.makeRunCommandAction(clickCommand))
                );
        });
    }

    private static void removeSumUpPlaceholderOnPlayerLeft(ServerPlayerEntity player) {
        SumUpPlaceholder.uuid2stats.remove(player.getUuidAsString());
    }

    @Override
    protected void onInitialize() {
        PlayerEvents.ON_PLAYER_LEAVE.register(PlaceholderInitializer::removeSumUpPlaceholderOnPlayerLeft);

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
            UpdateSumUpPlaceholderJob updateSumUpPlaceholderJob = new UpdateSumUpPlaceholderJob();
            Managers.getScheduleManager().scheduleJob(updateSumUpPlaceholderJob);
        });
    }

    private void registerDatePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("date", """
            Returns current `date`.
            Accept an optional argument to specify the `date format`.
            """);
        PlaceholderHelper.registerServerPlaceholder(descriptor, (server, arg) -> {
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
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("escape", """
            To escape the `placeholder` from the `text parser`.
            The `first argument` is the literal string of the `target placeholder`.
            The `second argument` is the integer for `escape levels`, by default is 1.
            """);
        PlaceholderHelper.registerServerPlaceholder(descriptor, (server, args) -> {
            if (args == null) return PlaceholderHelper.INVALID_ARGS_ERROR_TEXT;

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
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("protect", """
            Accept one `required string argument`, and returns the `literal text` of that.
            """);
        PlaceholderHelper.registerServerPlaceholder(descriptor, (server, args) -> {
            if (args == null) return Text.empty();
            return Text.literal(args);
        });
    }

    private void registerHasPermissionPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("has_permission", """
            The `first string argument` is the `permission` to check.
            Returns whether the `player` has that permission, in boolean.
            """);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player, args) -> {
            boolean value = LuckpermsHelper.hasPermission(player.getUuid(), new PermissionDescriptor(true, args, null));
            return Text.literal(String.valueOf(value));
        });
    }

    private void registerGetMetaPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("get_meta", """
            The `first string argument` is the `luckperms meta` key.
            Returns the `string` to represent the `meta value`.
            """);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player, args) -> {
            MetaDescriptor<String> tempMeta = new MetaDescriptor<>(true, args, String::valueOf, null);
            Optional<String> metaValue = LuckpermsHelper.getMeta(player.getUuid(), tempMeta);
            return Text.literal(metaValue.orElse("META_NOT_FOUND_ERROR"));
        });
    }

    private void registerRandomPlayerPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("random_player", """
            Pick a `random` player in `online` players, and returns its `name`.
            """);
        PlaceholderHelper.registerServerPlaceholder(descriptor, (server, args) -> {
            List<ServerPlayerEntity> playerList = ServerHelper.getOnlinePlayers();
            ServerPlayerEntity serverPlayerEntity = RandomUtil.drawList(playerList);
            return Text.literal(serverPlayerEntity.getGameProfile().getName());
        });
    }

    private void registerRandomPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("random", """
            The `first integer argument` is the `min value`.
            The `second integer argument` is the `max value`,
            Returns a random integer ranged `[min, max)`.
            """);
        PlaceholderHelper.registerServerPlaceholder(descriptor, (server, args) -> {
            if (args == null) return PlaceholderHelper.INVALID_ARGS_ERROR_TEXT;

            String[] split = args.split(" ");
            if (split.length != 2) return PlaceholderHelper.INVALID_ARGS_ERROR_TEXT;

            int i;
            try {
                i = RandomUtil.getRandom().nextInt(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            } catch (Exception e) {
                return PlaceholderHelper.INVALID_ARGS_ERROR_TEXT;
            }

            return Text.literal(String.valueOf(i));
        });
    }

    private void registerHealthBarPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("health_bar", """
            Returns the `health` of the `player`, in visual heart characters.
            """);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, player -> {
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

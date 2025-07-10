package io.github.sakurawald.fuji.module.initializer.placeholder;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
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


@Document(id = 1751826512394L, value = """
    Provides extra placeholders for `Text Placeholder API` mod.
    """)
@ColorBox(id = 1751978623242L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    ◉ Use `placeholder` in `language file`
    Actually, you can write the `placeholder` in the `language file`.
    The `contextual player` will be used to parse the placeholders.
    """)
@ColorBox(id = 1751978671933L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ Install more mods to provide extra placeholders.
    See https://placeholders.pb4.eu/user/mod-placeholders/
    """)



@CommandNode("placeholder")
@CommandRequirement(level = 4)
public class PlaceholderInitializer extends ModuleInitializer {

    private static final Map<String, Map<String, String>> ROTATE_CACHE = new HashMap<>();

    private static final Pattern ESCAPE_PARSER = Pattern.compile("\\s*([\\s\\S]+)\\s+(\\d+)\\s*");

    @Document(id = 1751826515140L, value = "List all placeholders registered in server.")
    @CommandNode("list")
    private static int $list(@CommandSource ServerPlayerEntity player) {
        List<Identifier> list = Placeholders.getPlaceholders().keySet().asList();
        new PlaceholderGui(player, list, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826519376L, value = "Parse a placeholder with a contextual player.")
    @CommandNode("parse")
    private static int $parse(@CommandSource ServerCommandSource source
        , Optional<ServerPlayerEntity> player
        , GreedyString input) {
        ServerPlayerEntity target = player.orElse(null);

        Text text = TextHelper.getTextByValue(target, input.getValue());
        source.sendMessage(text);
        return CommandHelper.Return.SUCCESS;
    }

    @DocStringProvider(id = 1751999849427L, value = """
        Returns the total playtime of all players in the server.
        """)
    private static void registerServerPlaytimePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("server_playtime", 1751999849427L);
        PlaceholderHelper.registerServerPlaceholder(descriptor, server -> Text.literal(String.valueOf(SumUpPlaceholder.ofServer().playtime)));
    }

    @DocStringProvider(id = 1751999873162L, value = """
        Returns the playtime of the player.
        """)

    private static void registerPlayerPlaytimePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("player_playtime", 1751999873162L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, player -> Text.literal(String.valueOf(SumUpPlaceholder.ofPlayer(player.getUuidAsString()).playtime)));
    }

    @DocStringProvider(id = 1751999885958L, value = """
        Returns the total distance traveled by all players in the server.
        """)
    private static void registerServerMovedPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("server_moved", 1751999885958L);
        PlaceholderHelper.registerServerPlaceholder(descriptor, server -> Text.literal(String.valueOf(SumUpPlaceholder.ofServer().moved)));
    }

    @DocStringProvider(id = 1751999903574L, value = """
        Returns the distance traveled by the player.
        """)
    private static void registerPlayerMovedPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("player_moved", 1751999903574L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, player -> Text.literal(String.valueOf(SumUpPlaceholder.ofPlayer(player.getUuidAsString()).moved)));
    }

    @DocStringProvider(id = 1751999917216L, value = """
        Returns the total entities killed by all players in the server.
        """)
    private static void registerServerKilledPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("server_killed", 1751999917216L);
        PlaceholderHelper.registerServerPlaceholder(descriptor, server -> Text.literal(String.valueOf(SumUpPlaceholder.ofServer().killed)));
    }

    @DocStringProvider(id = 1751999930297L, value = """
        Returns the killed entities by the player.
        """)
    private static void registerPlayerKilledPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("player_killed", 1751999930297L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, player -> Text.literal(String.valueOf(SumUpPlaceholder.ofPlayer(player.getUuidAsString()).killed)));
    }

    @DocStringProvider(id = 1751999941071L, value = """
        Returns the total placed blocks by all players in the server.
        """)
    private static void registerServerPlacedPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("server_placed", 1751999941071L);
        PlaceholderHelper.registerServerPlaceholder(descriptor, server -> Text.literal(String.valueOf(SumUpPlaceholder.ofServer().placed)));
    }

    @DocStringProvider(id = 1751999952462L, value = """
        Returns the placed blocks by the player.
        """)
    private static void registerPlayerPlacedPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("player_placed", 1751999952462L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, player -> Text.literal(String.valueOf(SumUpPlaceholder.ofPlayer(player.getUuidAsString()).placed)));
    }

    @DocStringProvider(id = 1751999963243L, value = """
        Returns the total mined blocks by all players in the server.
        """)
    private static void registerServerMinedPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("server_mined", 1751999963243L);
        PlaceholderHelper.registerServerPlaceholder(descriptor, (server) -> Text.literal(String.valueOf(SumUpPlaceholder.ofServer().mined)));
    }

    @DocStringProvider(id = 1751999973284L, value = """
        Returns the mined blocks by the player.
        """)
    private static void registerPlayerMinedPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("player_mined", 1751999973284L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, player -> Text.literal(String.valueOf(SumUpPlaceholder.ofPlayer(player.getUuidAsString()).mined)));
    }

    @DocStringProvider(id = 1751999986462L, value = """
        Returns the `luckperms prefix` of the player.
        """)
    public static void registerPrefixPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("player_prefix", 1751999986462L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player, arg) -> {
            String prefix = LuckpermsHelper.getPrefix(player.getUuid());
            return TextHelper.getTextByValue(player, prefix);
        });
    }

    @DocStringProvider(id = 1751999997365L, value = """
        Returns the `luckperms suffix` of the player.
        """)
    public static void registerSuffixPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("player_suffix", 1751999997365L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player, arg) -> {
            String prefix = LuckpermsHelper.getSuffix(player.getUuid());
            return TextHelper.getTextByValue(player, prefix);
        });
    }

    @DocStringProvider(id = 1752000048133L, value = """
        Returns the `location` of the player.
        """)
    public static void registerPosPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("pos", 1752000048133L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player) -> {
            int x = player.getBlockX();
            int y = player.getBlockY();
            int z = player.getBlockZ();
            String dim_name = player.getWorld().getRegistryKey().getValue().toString();
            String dim_display_name = TextHelper.Translator.getLanguageValueByKey(player, dim_name);
            String hoverString = TextHelper.Translator.getLanguageValueByKey(player, "chat.current_pos");
            switch (dim_name) {
                case "minecraft:overworld":
                    hoverString += "\n" + TextHelper.Translator.getLanguageValueByKey(player, "minecraft:the_nether")
                        + ": %d %s %d".formatted(x / 8, y, z / 8);
                    break;
                case "minecraft:the_nether":
                    hoverString += "\n" + TextHelper.Translator.getLanguageValueByKey(player, "minecraft:overworld")
                        + ": %d %s %d".formatted(x * 8, y, z * 8);
                    break;
            }

            String clickCommand = TextHelper.Translator.getLanguageValueByKey(player, "chat.xaero_waypoint_add.command");

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

    @DocStringProvider(id = 1752000061565L, value = """
        Returns current `date`.
        Accept an optional argument to specify the `date format`.
        """)
    private void registerDatePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("date", 1752000061565L);
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

    @DocStringProvider(id = 1752000074625L, value = """
        To escape the `placeholder` from the `text parser`.
        The `first argument` is the literal string of the `target placeholder`.
        The `second argument` is the integer for `escape levels`, by default is 1.
        """)
    private void registerEscapePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("escape", 1752000074625L);
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

    @DocStringProvider(id = 1752000094346L, value = """
        Accept one `required string argument`, and returns the `literal text` of that.
        """)
    private void registerProtectPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("protect", 1752000094346L);
        PlaceholderHelper.registerServerPlaceholder(descriptor, (server, args) -> {
            if (args == null) return Text.empty();
            return Text.literal(args);
        });
    }

    @DocStringProvider(id = 1752000110013L, value = """
        The `first string argument` is the `permission` to check.
        Returns whether the `player` has that permission, in boolean.
        """)
    private void registerHasPermissionPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("has_permission", 1752000110013L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player, args) -> {
            boolean value = LuckpermsHelper.hasPermission(player.getUuid(), new PermissionDescriptor(true, args, 0));
            return Text.literal(String.valueOf(value));
        });
    }

    @DocStringProvider(id = 1752000128157L, value = """
        The `first string argument` is the `luckperms meta` key.
        Returns the `string` to represent the `meta value`.
        """)
    private void registerGetMetaPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("get_meta", 1752000128157L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player, args) -> {
            MetaDescriptor<String> tempMeta = new MetaDescriptor<>(true, args, String::valueOf, 0);
            Optional<String> metaValue = LuckpermsHelper.getMeta(player.getUuid(), tempMeta);
            return Text.literal(metaValue.orElse("META_NOT_FOUND_ERROR"));
        });
    }

    @DocStringProvider(id = 1752000163280L, value = """
        Pick a `random` player in `online` players, and returns its `name`.
        """)
    private void registerRandomPlayerPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("random_player", 1752000163280L);
        PlaceholderHelper.registerServerPlaceholder(descriptor, (server, args) -> {
            List<ServerPlayerEntity> playerList = ServerHelper.getOnlinePlayers();
            ServerPlayerEntity serverPlayerEntity = RandomUtil.drawList(playerList);
            return Text.literal(serverPlayerEntity.getGameProfile().getName());
        });
    }

    @DocStringProvider(id = 1752000183895L, value = """
        The `first integer argument` is the `min value`.
        The `second integer argument` is the `max value`,
        Returns a random integer ranged `[min, max)`.
        """)
    private void registerRandomPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("random", 1752000183895L);
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

    @DocStringProvider(id = 1752000211964L, value = """
        Returns the `health` of the `player`, in visual heart characters.
        """)
    private void registerHealthBarPlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("health_bar", 1752000211964L);
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

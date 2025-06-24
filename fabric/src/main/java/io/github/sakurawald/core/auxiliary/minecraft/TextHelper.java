package io.github.sakurawald.core.auxiliary.minecraft;

import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.node.LiteralNode;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.NodeParser;
#if MC_VER <= MC_1_20_2
import eu.pb4.placeholders.api.parsers.TextParserV1;
import eu.pb4.placeholders.api.parsers.MarkdownLiteParserV1;
import eu.pb4.placeholders.api.Placeholders;
#elif MC_VER > MC_1_20_2
import eu.pb4.placeholders.api.parsers.tag.TagRegistry;
import eu.pb4.placeholders.api.parsers.tag.TextTag;
#endif

import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.core.config.Configs;
import io.github.sakurawald.core.config.handler.impl.ResourceConfigurationHandler;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class TextHelper {

    /* constants */
    public static final Text TEXT_NEWLINE = Text.of("\n");
    public static final Text TEXT_SPACE = Text.of(" ");
    public static final Text TEXT_EMPTY = Text.literal("");
    public static final String LANGUAGE_VALUE_PLACEHOLDER = "%message%";

    /* class states */
    private static final int ENSURE_THE_TAGS_ARE_REGISTERED_BEFORE_CREATING_THE_DEFAULT_PARSER = registerExtendedTags();
    public static final NodeParser POWERFUL_PARSER = makePowerfulParser();
    public static final NodeParser STYLE_ONLY_PARSER = makeStyleOnlyParser();
    public static final NodeParser PLACEHOLDER_ONLY_PARSER = makePlaceholderOnlyParser();

    private static final Map<String, String> player2code = new HashMap<>();
    private static final Map<String, JsonObject> code2json = new HashMap<>();
    private static final JsonObject UNSUPPORTED_LANGUAGE_MARKER = new JsonObject();

    private static final String SUPPRESS_SENDING_STRING_MARKER = "[suppress-sending]";
    private static final Text SUPPRESS_SENDING_TEXT_MARKER = Text.literal("[suppress-sending]");

    static {
        writeDefaultLanguageFilesIfAbsent();
    }

    private static NodeParser makeStyleOnlyParser() {
        #if MC_VER <= MC_1_20_2
            List<NodeParser> parsers = new ArrayList<>();
            parsers.add(TextParserV1.createDefault());
            parsers.add(MarkdownLiteParserV1.ALL);
            return NodeParser.merge(parsers);
        #elif MC_VER > MC_1_20_2
            return NodeParser.builder()
            .quickText()
            .simplifiedTextFormat()
            .markdown()
            .build();
        #endif
    }

    private static NodeParser makePowerfulParser() {
        #if MC_VER <= MC_1_20_2
        List<NodeParser> parsers = new ArrayList<>();
        parsers.add(TextParserV1.createDefault());
        parsers.add(Placeholders.DEFAULT_PLACEHOLDER_PARSER);
        parsers.add(MarkdownLiteParserV1.ALL);
        return NodeParser.merge(parsers);
        #elif MC_VER > MC_1_20_2
        return NodeParser.builder()
        .quickText()
        .simplifiedTextFormat()
        .globalPlaceholders()
        .markdown()
        .build();
        #endif
    }

    private static NodeParser makePlaceholderOnlyParser() {
        // NOTE: The placeholder-only parser should only parse placeholders, for command executing. it should not parse the <red> tags or other tags.

        #if MC_VER <= MC_1_20_2
        List<NodeParser> parsers = new ArrayList<>();
        parsers.add(Placeholders.DEFAULT_PLACEHOLDER_PARSER);
        return NodeParser.merge(parsers);
        #elif MC_VER > MC_1_20_2
        return NodeParser.builder()
        .globalPlaceholders().build();
        #endif
    }

    private static int registerExtendedTags() {
        #if MC_VER <= MC_1_20_2
        TextParserV1.registerDefault(
            TextParserV1.TextTag.of(
                "newline",
                List.of("newline"),
                "formatting",
                true,
                (tag, data, input, handlers, endAt) -> new TextParserV1.TagNodeValue(new LiteralNode("\n"), 0)
            )
        );

        #elif MC_VER > MC_1_20_2
        TagRegistry.registerDefault(
            TextTag.self(
                "newline",
                "formatting",
                true,
                (nodes, data, parser) -> new LiteralNode("\n")
            )
        );
        #endif

        return 0;
    }

    private static void writeDefaultLanguageFilesIfAbsent() {
        for (String languageFile : ReflectionUtil.getGraph(ReflectionUtil.LANGUAGE_GRAPH_FILE_NAME)) {
            new ResourceConfigurationHandler("lang/" + languageFile).readStorage();
        }
    }

    /**
     * Clear the language file loaded into the memory.
     * Note that once the attempt to load a language file from storage is failed, a JsonObject marker named `UNSUPPORTED LANGUAGE` will be put into the map, leading the subsequent attempts simply return the marker.
     */
    public static void clearLoadedLanguageJsons() {
        code2json.clear();
    }

    public static void setClientSideLanguageCode(String playerName, String languageRepresentationUsedByMojang) {
        // mojang network protocol use a strange language representation, mojang use `en_us` instead of `en_US`
        player2code.put(playerName, convertToLanguageCode(languageRepresentationUsedByMojang));
    }

    private static void loadLanguageJsonIfAbsent(String languageCode) {
        if (code2json.containsKey(languageCode)) return;

        try {
            String languageFile = languageCode + ".json";
            ResourceConfigurationHandler resourceConfigurationHandler = new ResourceConfigurationHandler("lang/" + languageFile);

            //read it
            resourceConfigurationHandler.readStorage();

            code2json.put(languageCode, resourceConfigurationHandler.model().getAsJsonObject());
            LogUtil.info("Language {} loaded.", languageCode);
        } catch (Exception e) {
            code2json.put(languageCode, UNSUPPORTED_LANGUAGE_MARKER);
            LogUtil.warn("Failed to load language `{}`", languageCode);
        }
    }

    private String convertToLanguageCode(String input) {
        if (input == null || !input.contains("_")) {
            return input;
        }

        String[] parts = input.split("_");

        String language = parts[0].toLowerCase();
        String region = parts[1].toUpperCase();
        return language + "_" + region;
    }

    private @NotNull String getClientSideLanguageCode(@Nullable Object audience) {
        if (audience == null) return getDefaultLanguageCode();

        PlayerEntity player = null;
        if (audience instanceof ServerPlayerEntity) {
            player = ((ServerPlayerEntity) audience);
        }
        else if (audience instanceof PlayerEntity) {
            player = (PlayerEntity) audience;
        }
        else if (audience instanceof ServerCommandSource) {
            ServerCommandSource commandSource = (ServerCommandSource) audience;
            if (commandSource.getPlayer() != null) {
                player = commandSource.getPlayer();
            }
        }

        // always use default_language for non-player object.
        if (player == null) return getDefaultLanguageCode();

        return player2code.getOrDefault(player.getGameProfile().getName(), getDefaultLanguageCode());
    }

    private @NotNull JsonObject getLanguageJsonObject(String languageCode) {
        // load language object from disk for the first time
        loadLanguageJsonIfAbsent(languageCode);

        return code2json.get(languageCode);
    }

    private static String getDefaultLanguageCode() {
        // allow user to write `en_us` in `config.json`.
        return convertToLanguageCode(Configs.mainControlConfig.model().core.language.default_language);
    }

    private static boolean isDefaultLanguageCode(String languageCode) {
        return languageCode.equals(getDefaultLanguageCode());
    }

    public static @NotNull String getValueByKey(@Nullable Object audience, String key, Object... args) {
        String value = getValueByKey(audience, key);
        return resolveArgs(value, args);
    }

    public static @NotNull String getValueByKey(@Nullable Object audience, String key) {
        String languageCode = getClientSideLanguageCode(audience);

        String value = getValue(languageCode, key);
        if (value != null) return value;

        // always fallback string for missing keys
        String fallbackValue = "(no key `%s` in language `%s`)".formatted(key, languageCode);
        LogUtil.warn("{} triggered by {}", fallbackValue, audience);
        return fallbackValue;
    }

    private static @Nullable String getValue(String languageCode, String key) {
        /* get json */
        JsonObject languageJson = getLanguageJsonObject(languageCode);

        /* use fallback language if the client-side language is not supported in the server-side. */
        if (languageJson == UNSUPPORTED_LANGUAGE_MARKER) {
            languageCode = getDefaultLanguageCode();
            languageJson = getLanguageJsonObject(languageCode);
        }

        /* get value */
        if (languageJson.has(key)) {
            return languageJson.get(key).getAsString();
        }

        // use partial locale
        if (!isDefaultLanguageCode(languageCode)) {
            return getValue(getDefaultLanguageCode(), key);
        }

        // if the language key is missing in the default language, then we have nothing to do.
        return null;
    }


    private static @NotNull String resolveArgs(@NotNull String string, Object... args) {
        if (args.length > 0) {
            try {
                return String.format(string, args);
            } catch (Exception e) {
                LogUtil.warn("""
                    Failed to resolve args for language value `{}` with args `{}`

                    It's like a syntax mistake in the language file.
                    """, string, args);
            }
        }
        return string;
    }

    public static String visitString(Text text) {
        return text.getString();
    }

    public static @NotNull String parsePlaceholder(@Nullable Object audience, String value) {
        return visitString(TextHelper.getText(PLACEHOLDER_ONLY_PARSER, audience, false, value));
    }

    /* This is the core method to map `String` into `Text`.
     *  All methods that return `Vomponent` are converted from this method.
     * */
    public static @NotNull Text getText(@NonNull NodeParser parser, @Nullable Object audience, boolean isKey, String keyOrValue, Object... args) {
        String value = isKey ? getValueByKey(audience, keyOrValue) : keyOrValue;

        // suppress this sending?
        if (value.equals(SUPPRESS_SENDING_STRING_MARKER)) {
            return SUPPRESS_SENDING_TEXT_MARKER;
        }

        // resolve args
        value = resolveArgs(value, args);

        PlaceholderContext placeholderContext = makePlaceholderContext(audience);
        ParserContext parserContext = ParserContext.of(PlaceholderContext.KEY, placeholderContext);

        return parser.parseText(TextNode.of(value), parserContext);
    }

    private static @NotNull PlaceholderContext makePlaceholderContext(@Nullable Object audience) {
        /* extract the player from source */
        if (audience instanceof ServerCommandSource) {
            audience = ((ServerCommandSource) audience).getPlayer();
        }

        /* case type */
        PlaceholderContext placeholderContext;
        if (audience instanceof PlayerEntity playerEntity) {
            placeholderContext = PlaceholderContext.of(playerEntity);
        } else {
            placeholderContext = PlaceholderContext.of(ServerHelper.getServer());
        }

        return placeholderContext;
    }

    private static @NotNull Text getText(@Nullable Object audience, boolean isKey, String keyOrValue, Object... args) {
        return getText(POWERFUL_PARSER, audience, isKey, keyOrValue, args);
    }

    public static @NotNull Text getTextByKey(@Nullable Object audience, String key, Object... args) {
        return getText(audience, true, key, args);
    }

    public static String getKeywordValue(@Nullable Object audience, String keyword) {
        return getValueByKey(audience, "keyword." + keyword);
    }

    public static MutableText getTextByKeyWithKeyword(@Nullable Object audience, String key, String keyword) {
        String replacement = getKeywordValue(audience, keyword);
        String value = getValueByKey(audience, key, replacement);
        return Text.literal(value);
    }

    public static @NotNull Text getTextByValue(@Nullable Object audience, String value, Object... args) {
        return getText(audience, false, value, args);
    }

    private static @NotNull List<Text> getTextList(@Nullable Object audience, boolean isKey, String keyOrValue) {
        String lines = isKey ? getValueByKey(audience, keyOrValue) : keyOrValue;

        List<Text> ret = new ArrayList<>();
        for (String line : lines.split("\n|<newline>")) {
            ret.add(getTextByValue(audience, line));
        }
        return ret;
    }

    public static @NotNull List<Text> getTextListByKey(@Nullable Object audience, String key) {
        return getTextList(audience, true, key);
    }

    public static @NotNull List<Text> getTextListByValue(@Nullable Object audience, String value) {
        return getTextList(audience, false, value);
    }

    public static void sendMessageByFlag(@NotNull Object audience, boolean flag) {
        sendMessageByKey(audience, flag ? "on" : "off");
    }

    public static void sendMessageByKey(@NotNull Object audience, String key, Object... args) {
        Text text = getTextByKey(audience, key, args);

        /* suppress this sending ? */
        if (text == SUPPRESS_SENDING_TEXT_MARKER) {
            LogUtil.debug("Suppress the sending of message: audience = {}, key = {}, args = {}", audience, key, args);
            return;
        }


        /* extract the source */
        if (audience instanceof CommandContext<?> ctx) {
            audience = ctx.getSource();
        }

        /* dispatch by type */
        if (audience instanceof PlayerEntity playerEntity) {
            playerEntity.sendMessage(text, false);
            return;
        }

        if (audience instanceof ServerCommandSource serverCommandSource) {
            serverCommandSource.sendMessage(text);
            return;
        }

        LogUtil.error("""
            Can't send message to unknown audience type: {}
            Key: {}
            Args: {}
            """, audience == null ? null : audience.getClass().getName(), key, args);
    }

    public static void sendActionBarByKey(@NotNull ServerPlayerEntity player, String key, Object... args) {
        player.sendMessage(getTextByKey(player, key, args), true);
    }

    public static void sendBroadcastByKey(@NotNull String key, Object... args) {
        // fix: log broadcast for console
        Text text = getTextByKey(null, key, args);
        LogUtil.info(visitString(text));

        for (ServerPlayerEntity player : ServerHelper.getServer().getPlayerManager().getPlayerList()) {
            TextHelper.sendMessageByKey(player, key, args);
        }
    }

    private static String visitString(TextContent textContent) {
        StringBuilder stringBuilder = new StringBuilder();
        textContent.visit(string -> {
            stringBuilder.append(string);
            return Optional.empty();
        });
        return stringBuilder.toString();
    }

    public static MutableText replaceTextWithMarker(Text text, String marker, Supplier<Text> replacementSupplier) {
        return replaceTextWithRegex(text, "\\[%s\\]".formatted(marker), replacementSupplier);
    }

    public static MutableText replaceTextWithRegex(Text text, String regex, Supplier<Text> nonMemorizedReplacementSupplier) {
        // memorize the supplier
        nonMemorizedReplacementSupplier = memoizeSupplier(nonMemorizedReplacementSupplier);

        return replaceText(text, Pattern.compile(regex), nonMemorizedReplacementSupplier);
    }

    private static MutableText replaceText(Text text, Pattern pattern, Supplier<Text> replacementSupplier) {
        MutableText replacedText;

        /* process the atom */
        String textString = visitString(text.getContent());
        @Nullable List<Text> splits = trySplitString(textString, pattern, replacementSupplier);

        if (splits == null) {
            replacedText = text.copyContentOnly();
        } else {
            // use a dummy root to represent the replaced node.
            MutableText dummyRoot = Text.empty();
            replacedText = dummyRoot;
            splits.forEach(dummyRoot::append);
        }
        replacedText.fillStyle(text.getStyle());

        /* go down */
        for (Text sibling : text.getSiblings()) {
            MutableText replacedSibling = replaceText(sibling, pattern, replacementSupplier);
            replacedText.append(replacedSibling);
        }

        return replacedText;
    }

    private static @Nullable List<Text> trySplitString(String string, Pattern pattern, Supplier<Text> replacementSupplier) {
        /* quick return */
        Matcher matcher = pattern.matcher(string);

        List<Text> ret = new ArrayList<>();
        int startIndex = 0;
        while (matcher.find()) {
            int i = matcher.start();

            // append the head text if exists
            if (i != startIndex) {
                ret.add(Text.literal(string.substring(startIndex, i)));
            }

            // append the replacement text
            ret.add(replacementSupplier.get());

            startIndex = matcher.end();
        }

        // return null if nothing is replaced.
        if (ret.isEmpty()) return null;

        /* append the tail string if exists */
        if (startIndex < string.length()) {
            ret.add(Text.literal(string.substring(startIndex)));
        }

        return ret;
    }

    private static <T> Supplier<T> memoizeSupplier(Supplier<T> delegate) {
        AtomicReference<T> value = new AtomicReference<>();
        return () -> {
            T val = value.get();
            if (val == null) {
                val = value.updateAndGet(cur -> cur == null ?
                    Objects.requireNonNull(delegate.get()) : cur);
            }
            return val;
        };
    }

    public static void sendBroadcastByValue(Text text) {
        LogUtil.info(visitString(text));

        for (ServerPlayerEntity player : ServerHelper.getPlayers()) {
            player.sendMessage(text);
        }
    }

    public static void sendTitle(@NotNull ServerPlayerEntity player, @NotNull Text mainTitle, @NotNull Text subTitle) {
        sendTitle(player,10,70,20, mainTitle, subTitle);
    }

    public static void sendTitle(ServerPlayerEntity player, int fadeInTicks, int stayTicks, int fadeOutTicks, Text mainTitle, Text subTitle) {
        player.networkHandler.sendPacket(new TitleFadeS2CPacket(fadeInTicks, stayTicks, fadeOutTicks));
        player.networkHandler.sendPacket(new TitleS2CPacket(mainTitle));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(subTitle));
    }

    public static class ClickEvent {

        public static net.minecraft.text.ClickEvent makeRunCommandAction(String command) {
            #if MC_VER <= MC_1_21_4
                return new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, command);
            #elif MC_VER >= MC_1_21_5
                return new net.minecraft.text.ClickEvent.RunCommand(command);
            #endif
        }

        public static net.minecraft.text.ClickEvent makeCopyToClipboardAction(String string) {
            #if MC_VER <= MC_1_21_4
                return new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.COPY_TO_CLIPBOARD, string);
            #elif MC_VER >= MC_1_21_5
                return new net.minecraft.text.ClickEvent.CopyToClipboard(string);
            #endif
        }
    }

    public static class HoverEvent {

        public static net.minecraft.text.HoverEvent makeShowTextAction(Text hoverText) {
            #if MC_VER <= MC_1_21_4
            return new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, hoverText);
            #elif MC_VER >= MC_1_21_5
                return new net.minecraft.text.HoverEvent.ShowText(hoverText);
            #endif
        }
    }

    public static Text fromJson(String tagString) {
        #if MC_VER <= MC_1_20_2
            return Text.Serializer.fromJson(tagString);
        #elif MC_VER > MC_1_20_2 && MC_VER < MC_1_20_5
            return Text.Serialization.fromJson(tagString);
        #elif MC_VER >= MC_1_20_5
            throw new UnsupportedOperationException();
        #endif
    }

    public static String toJson(Text text) {
        #if MC_VER <= MC_1_20_2
            return Text.Serializer.toJson(text);
        #elif MC_VER > MC_1_20_2 && MC_VER < MC_1_20_5
            return Text.Serialization.toJsonString(text);
        #elif MC_VER >= MC_1_20_5
            throw new UnsupportedOperationException();
        #endif
    }

    public static String escapeTags(String string) {
        // NOTE: Here are special characters for Text Placeholder API parsers.
        return string
            .replace("<", "\\<")
            .replace(">", "\\>")
            .replace("*","\\*");
    }

    public static Text parseString(NodeParser parser, String input) {
        return parser.parseNode(input).toText();
    }

    public static String decorateDocumentString(String documentString) {
        return Arrays.stream(documentString
                .split("\n"))
            .map(line -> "<#FFA1F5>" + line)
            .collect(Collectors.joining("\n"));
    }

    public static Text getDocumentText(Object audience, String value) {
        value = decorateDocumentString(value);
        return getTextByValue(audience, value);
    }

    public static List<Text> getDocumentTextList(Object audience, String value) {
        value = decorateDocumentString(value);
        return getTextListByValue(audience, value);
    }
}

package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
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

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.config.Configs;
import io.github.sakurawald.fuji.core.config.handler.impl.ResourceConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.service.url_highlighter.UrlHighlighter;

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

@ForDeveloper("""
    The design of language system.
    1. Use hash map to map the language key into language value.
    2. Teh language value should be simple enough. (Reduce the usage of long sentence)
    3. Reduce the usage of `click event` and `hover event` tag in language value. (Use programmatically way to attach them)

    """)
public class TextHelper {

    /* Constants. */
    public static final Text TEXT_NEWLINE = Text.of("\n");
    public static final Text TEXT_SPACE = Text.of(" ");
    public static final Text TEXT_EMPTY = Text.literal("");
    public static final String MESSAGE_PLACEHOLDER = "%message%";

    private static final Map<String, String> PLAYER_2_LANGUAGE_CODE = new HashMap<>();
    private static final Map<String, JsonObject> LANGUAGE_CODE_2_LANGUAGE_JSON = new HashMap<>();
    private static final JsonObject UNSUPPORTED_LANGUAGE_MARKER = new JsonObject();

    private static final String SUPPRESS_SENDING_STRING_MARKER = "[suppress-sending]";
    private static final Text SUPPRESS_SENDING_TEXT_MARKER = Text.literal("[suppress-sending]");

    private static final String LANGUAGE_FILE_PATH = "lang/";

    static {
        Loader.writeDefaultLanguageFilesIfAbsent();
    }

    @ForDeveloper("The functions used to interact with the Text Parser.")
    public static class Parsers {
        private static final int THIS_STATIC_VARIABLE_IS_USED_TO_ENSURE_THE_EXTENDED_TAGS_ARE_REGISTERED_BEFORE_CREATING_THE_DEFAULT_PARSER = registerExtendedTags();
        public static final NodeParser POWERFUL_PARSER = makePowerfulParser();
        public static final NodeParser STYLE_ONLY_PARSER = makeStyleOnlyParser();
        public static final NodeParser PLACEHOLDER_ONLY_PARSER = makePlaceholderOnlyParser();

        @ForDeveloper("The style-only parser should support mini-message language and markdown language.")
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
            // NOTE: The placeholder-only parser should only parse placeholders, for command executing. It should not parse the <red> tags or other tags.

            #if MC_VER <= MC_1_20_2
            List<NodeParser> parsers = new ArrayList<>();
            parsers.add(Placeholders.DEFAULT_PLACEHOLDER_PARSER);
            return NodeParser.merge(parsers);
            #elif MC_VER > MC_1_20_2
            return NodeParser
                .builder()
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

        public static String escapeTags(String string) {
            // NOTE: Here are special characters for Text Placeholder API parsers.
            return string
                .replace("<", "\\<")
                .replace(">", "\\>")
                .replace("*","\\*")
                .replace("%", "\\%");
        }

        public static Text parseString(NodeParser parser, String input) {
            return parser.parseNode(input).toText();
        }

        private static @NotNull PlaceholderContext makePlaceholderContext(@Nullable Object audience) {
            /* Unbox the player from server command source. */
            if (audience instanceof ServerCommandSource) {
                audience = ((ServerCommandSource) audience).getPlayer();
            }

            /* Dispatch the audience type, and provide the bits to make the placeholder context. */
            PlaceholderContext placeholderContext;
            if (audience instanceof PlayerEntity playerEntity) {
                placeholderContext = PlaceholderContext.of(playerEntity);
            } else if (audience instanceof GameProfile gameProfile) {
                placeholderContext = PlaceholderContext.of(gameProfile, ServerHelper.getServer());
            } else {
                placeholderContext = PlaceholderContext.of(ServerHelper.getServer());
            }

            return placeholderContext;
        }

        public static @NotNull String parsePlaceholderString(@Nullable Object audience, String value) {
            Text text = TextHelper.getText(PLACEHOLDER_ONLY_PARSER, audience, false, value);
            return Operators.visitString(text);
        }
    }

    @ForDeveloper("The functions used to load language file from storage into memory, and resolve the suitable language json for given audience.")
    public static class Loader {
        private static void writeDefaultLanguageFilesIfAbsent() {
            for (String languageFileName : ReflectionUtil.getCompileTimeGraph(ReflectionUtil.LANGUAGE_GRAPH_FILE_NAME)) {
                new ResourceConfigurationHandler(LANGUAGE_FILE_PATH + languageFileName)
                    .readStorage();
            }
        }

        @ForDeveloper("""
            Clear the loaded language file in memory.
            Note that once teh attempt to load a language file from storage is failed, a JsonObject maker named `UNSUPPORTED LANGUAGE` will be put into the map.
            Leading the subsequent attempts imply returns the marker.
            """)
        public static void clearLoadedLanguageJsons() {
            LANGUAGE_CODE_2_LANGUAGE_JSON.clear();
        }

        public static void setClientSideLanguageCode(String playerName, String languageRepresentationUsedByMojang) {
            // NOTE: Mojang network protocol use a stupid language representation, mojang use `en_us` instead of `en_US`, so we need to unify it.
            String unifiedLanguageCode = unifyLanguageCode(languageRepresentationUsedByMojang);
            PLAYER_2_LANGUAGE_CODE.put(playerName, unifiedLanguageCode);
        }

        private static void loadLanguageJsonIfAbsent(String languageCode) {
            // Skip reading the language file from storage, if there is language json in memory.
            // NOTE: The language json can be UNSUPPORTED_LANGUAGE json marker.
            if (LANGUAGE_CODE_2_LANGUAGE_JSON.containsKey(languageCode)) {
                return;
            }

            try {
                String languageFileName = languageCode + ".json";
                ResourceConfigurationHandler resourceConfigurationHandler = new ResourceConfigurationHandler(LANGUAGE_FILE_PATH + languageFileName);
                resourceConfigurationHandler.readStorage();

                LANGUAGE_CODE_2_LANGUAGE_JSON.put(languageCode, resourceConfigurationHandler.model().getAsJsonObject());
                LogUtil.info("Language {} loaded.", languageCode);
            } catch (Exception e) {
                LANGUAGE_CODE_2_LANGUAGE_JSON.put(languageCode, UNSUPPORTED_LANGUAGE_MARKER);
                LogUtil.error("Failed to load language `{}` from storage.", languageCode, e);
            }
        }

        private static String unifyLanguageCode(String input) {
            if (input == null || !input.contains("_")) {
                return input;
            }

            String[] components = input.split("_");
            String language = components[0].toLowerCase();
            String region = components[1].toUpperCase();
            return language + "_" + region;
        }

        private static @NotNull String getClientSideLanguageCode(@Nullable Object audience) {
            // If audience is null, just use the default language.
            if (audience == null) {
                return getDefaultLanguageCode();
            }

            // If audience is non-null, but we have no clue what is the source player, then still use the default language.
            @Nullable PlayerEntity player = tryExtractPlayerEntity(audience);
            if (player == null) return getDefaultLanguageCode();

            // Get the language code used by the player.
            String playerName = PlayerHelper.getPlayerName(player);
            String defaultLanguageCode = getDefaultLanguageCode();
            return PLAYER_2_LANGUAGE_CODE.getOrDefault(playerName, defaultLanguageCode);
        }

        @SuppressWarnings({"IfCanBeSwitch", "PatternVariableCanBeUsed"})
        private static @Nullable PlayerEntity tryExtractPlayerEntity(@NotNull Object audience) {
            PlayerEntity player = null;

            if (audience instanceof ServerPlayerEntity) {
                player = ((ServerPlayerEntity) audience);
            } else if (audience instanceof PlayerEntity) {
                player = (PlayerEntity) audience;
            } else if (audience instanceof ServerCommandSource) {
                ServerCommandSource commandSource = (ServerCommandSource) audience;
                if (commandSource.getPlayer() != null) {
                    player = commandSource.getPlayer();
                }
            }
            return player;
        }

        private static @NotNull JsonObject getLanguageJsonObject(String languageCode) {
            // Ensure the language json is loaded into memory.
            loadLanguageJsonIfAbsent(languageCode);

            // Get the language json object from memory.
            return LANGUAGE_CODE_2_LANGUAGE_JSON.get(languageCode);
        }

        private static String getDefaultLanguageCode() {
            // NOTE: Unify the user input language code, to allow the user writes `en_us` to mean `en_US`.
            return unifyLanguageCode(Configs.MAIN_CONTROL_CONFIG.model().core.language.default_language);
        }

        private static boolean isDefaultLanguageCode(String languageCode) {
            return languageCode.equals(getDefaultLanguageCode());
        }

    }

    public static @NotNull String getValueByKey(@Nullable Object audience, String key, Object... args) {
        String value = getValueByKey(audience, key);
        return resolveArgs(value, args);
    }

    public static @NotNull String getValueByKey(@Nullable Object audience, String key) {
        String languageCode = Loader.getClientSideLanguageCode(audience);

        String value = getValue(languageCode, key);
        if (value != null) return value;

        // always fallback string for missing keys
        String fallbackValue = "(no key `%s` in language `%s`)".formatted(key, languageCode);
        LogUtil.warn("{} triggered by {}", fallbackValue, audience);
        return fallbackValue;
    }

    private static @Nullable String getValue(String languageCode, String key) {
        /* get json */
        JsonObject languageJson = Loader.getLanguageJsonObject(languageCode);

        /* use fallback language if the client-side language is not supported in the server-side. */
        if (languageJson == UNSUPPORTED_LANGUAGE_MARKER) {
            languageCode = Loader.getDefaultLanguageCode();
            languageJson = Loader.getLanguageJsonObject(languageCode);
        }

        /* get value */
        if (languageJson.has(key)) {
            return languageJson.get(key).getAsString();
        }

        // use partial locale
        if (!Loader.isDefaultLanguageCode(languageCode)) {
            return getValue(Loader.getDefaultLanguageCode(), key);
        }

        // if the language key is missing in the default language, then we have nothing to do.
        return null;
    }

    private static @NotNull String resolveArgs(@NotNull String string, Object... args) {
        if (args.length > 0) {
            try {
                return String.format(string, args);
            } catch (Exception e) {
                LogUtil.error("""
                    Failed to resolve args for language value `{}` with args `{}`

                    It's like a syntax mistake in the language file.
                    """, string, args, e);
            }
        }
        return string;
    }

    public static class Operators {

        private static String visitString(TextContent textContent) {
            StringBuilder stringBuilder = new StringBuilder();
            textContent.visit(string -> {
                stringBuilder.append(string);
                return Optional.empty();
            });
            return stringBuilder.toString();
        }

        public static String visitString(Text text) {
            return text.getString();
        }

        public static MutableText replaceTextWithMarker(Text text, String marker, Supplier<Text> replacementSupplier) {
            return replaceTextWithRegex(text, "\\[%s\\]".formatted(marker), replacementSupplier);
        }

        public static MutableText replaceTextWithRegex(Text text, String regex, Supplier<Text> nonMemorizedReplacementSupplier) {
            // memorize the supplier
            nonMemorizedReplacementSupplier = makeMemoizeSupplier(nonMemorizedReplacementSupplier);

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

        private static <T> Supplier<T> makeMemoizeSupplier(Supplier<T> delegate) {
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
    }

    @ForDeveloper("This is the core method to map String into Text.")
    public static @NotNull Text getText(@NotNull NodeParser parser, @Nullable Object audience, boolean isKey, String keyOrValue, Object... args) {
        String value = isKey ? getValueByKey(audience, keyOrValue) : keyOrValue;

        // check NPE.
        if (value == null) {
            return Text.literal("Value is null: %s".formatted(keyOrValue));
        }

        // suppress this sending?
        if (value.equals(SUPPRESS_SENDING_STRING_MARKER)) {
            return SUPPRESS_SENDING_TEXT_MARKER;
        }

        // resolve args
        value = resolveArgs(value, args);

        PlaceholderContext placeholderContext = Parsers.makePlaceholderContext(audience);
        ParserContext parserContext = ParserContext.of(PlaceholderContext.KEY, placeholderContext);

        return parser.parseText(TextNode.of(value), parserContext);
    }

    public static @NotNull Text getTextByKey(@Nullable Object audience, String key, Object... args) {
        return getText(Parsers.POWERFUL_PARSER, audience, true, key, args);
    }

    public static @NotNull Text getTextByValue(@Nullable Object audience, String value, Object... args) {
        return getText(Parsers.POWERFUL_PARSER, audience, false, value, args);
    }

    public static String getValueByKeyword(@Nullable Object audience, String keyword) {
        String key = "keyword." + keyword;
        return getValueByKey(audience, key);
    }

    public static MutableText getTextByKeyWithKeyword(@Nullable Object audience, String key, String keyword) {
        String replacement = getValueByKeyword(audience, keyword);
        String value = getValueByKey(audience, key, replacement);
        return Text.literal(value);
    }

    private static @NotNull List<Text> getTextList(@Nullable Object audience, boolean isKey, String keyOrValue) {
        String value = isKey ? getValueByKey(audience, keyOrValue) : keyOrValue;

        List<Text> lines = new ArrayList<>();
        for (String line : splitStringLines(value)) {
            lines.add(getTextByValue(audience, line));
        }
        return lines;
    }

    private static String[] splitStringLines(String value) {
        return value.split("\n|<newline>");
    }

    public static @NotNull List<Text> getTextListByKey(@Nullable Object audience, String key) {
        return getTextList(audience, true, key);
    }

    public static @NotNull List<Text> getTextListByValue(@Nullable Object audience, String value) {
        return getTextList(audience, false, value);
    }

    public static class Getter {

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

        /* Unbox the command context, to get the command source. */
        if (audience instanceof CommandContext<?> ctx) {
            audience = ctx.getSource();
        }

        /* Dispatch the message sending method, by audience type. */
        if (audience instanceof ServerPlayerEntity serverPlayerEntity) {
            sendMessageToServerPlayerEntity(serverPlayerEntity, text);
            return;
        }

        if (audience instanceof PlayerEntity playerEntity) {
            sendMessageToPlayerEntity(playerEntity, text);
            return;
        }

        if (audience instanceof ServerCommandSource serverCommandSource) {
            if (serverCommandSource.getPlayer() != null) {
                ServerPlayerEntity player = serverCommandSource.getPlayer();
                sendMessageToServerPlayerEntity(player, text);
                return;
            }
            sendMessageToServerCommandSource(serverCommandSource, text);
            return;
        }

        /* Unknown audience type. */
        LogUtil.error("""
            Can't send message to unknown audience type: {}
            Key: {}
            Args: {}
            """, audience == null ? null : audience.getClass().getName(), key, args);
    }

    private static void sendMessageToPlayerEntity(PlayerEntity playerEntity, Text text) {
        playerEntity.sendMessage(text, false);
    }

    private static void sendMessageToServerCommandSource(ServerCommandSource serverCommandSource, Text text) {
        serverCommandSource.sendMessage(text);
    }

    private static void sendMessageToServerPlayerEntity(ServerPlayerEntity serverPlayerEntity, Text text) {
        if (serverPlayerEntity.networkHandler == null) {
            LogUtil.warn("Failed to send the message to player {}, because its network handler is null. (Is it an dummy offline player entity?): text = {}", serverPlayerEntity, text);
            return;
        }
        serverPlayerEntity.sendMessage(text, false);
    }

    public static void sendActionBarByKey(@NotNull ServerPlayerEntity player, String key, Object... args) {
        player.sendMessage(getTextByKey(player, key, args), true);
    }

    public static void sendBroadcastByKey(@NotNull String key, Object... args) {
        // fix: log broadcast for console
        Text text = getTextByKey(null, key, args);
        LogUtil.info(Operators.visitString(text));

        for (ServerPlayerEntity player : ServerHelper.getServer().getPlayerManager().getPlayerList()) {
            TextHelper.sendMessageByKey(player, key, args);
        }
    }

    public static void sendBroadcastByValue(Text text) {
        LogUtil.info(Operators.visitString(text));

        for (ServerPlayerEntity player : ServerHelper.getOnlinePlayers()) {
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

    private static String decorateDocumentString(String documentString) {
        String decoratedDocumentString = Arrays.stream(documentString
                .split("\n"))
            .map(line -> "<#FFA1F5>" + line)
            .collect(Collectors.joining("\n"));

        decoratedDocumentString = UrlHighlighter.highlight(decoratedDocumentString);
        return decoratedDocumentString;
    }

    public static Text getDocumentText(Object audience, String  value) {
        value = decorateDocumentString(value);
        return getText(Parsers.STYLE_ONLY_PARSER, audience, false, value);
    }

    public static List<Text> getDocumentTextList(Object audience, String value) {
        return Arrays.stream(splitStringLines(value))
            .map(line -> getDocumentText(audience, line))
            .toList();
    }

    @ForDeveloper("The abstraction for text events.")
    public static class Events {

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
    }
}


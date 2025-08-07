package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.JsonOps;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.node.LiteralNode;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.NodeParser;
import io.github.sakurawald.fuji.core.auxiliary.JsonUtil;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.auxiliary.StringUtil;
import io.github.sakurawald.fuji.core.config.Configs;
import io.github.sakurawald.fuji.core.config.handler.impl.LanguageConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ResourceConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.document.auxiliary.DocumentUtil;
import io.github.sakurawald.fuji.core.structure.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public static final String PRIMARY_COLOR_STRING = "<#FFA1F5>";
    public static final int PRIMARY_COLOR_INT = 16753141;

    static {
        Loader.writeDefaultLanguageFilesIfAbsent();
    }

    @ForDeveloper("The functions used to interact with the Text Parser.")
    public static class Parsers {
        @SuppressWarnings("unused")
        private static final int THIS_STATIC_VARIABLE_IS_USED_TO_ENSURE_THE_EXTENDED_TAGS_ARE_REGISTERED_BEFORE_CREATING_THE_DEFAULT_PARSER = registerExtendedTags();
        public static final NodeParser POWERFUL_PARSER = makePowerfulParser();
        public static final NodeParser STYLE_ONLY_PARSER = makeStyleOnlyParser();
        public static final NodeParser MINI_MESSAGE_ONLY_PARSER = makeMiniMessageOnlyParser();
        public static final NodeParser PLACEHOLDER_ONLY_PARSER = makePlaceholderOnlyParser();

        private static NodeParser makeMiniMessageOnlyParser() {
            #if MC_VER <= MC_1_20_2
            List<NodeParser> parsers = new ArrayList<>();
            parsers.add(eu.pb4.placeholders.api.parsers.TextParserV1.createDefault());
            return NodeParser.merge(parsers);
            #elif MC_VER > MC_1_20_2
            return NodeParser.builder()
                .quickText()
                .simplifiedTextFormat()
                .build();
            #endif
        }

        @ForDeveloper("The style-only parser should support mini-message language and markdown language.")
        private static NodeParser makeStyleOnlyParser() {
            #if MC_VER <= MC_1_20_2
            List<NodeParser> parsers = new ArrayList<>();
            parsers.add(eu.pb4.placeholders.api.parsers.TextParserV1.createDefault());
            parsers.add(eu.pb4.placeholders.api.parsers.MarkdownLiteParserV1.ALL);
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
            parsers.add(eu.pb4.placeholders.api.parsers.TextParserV1.createDefault());
            parsers.add(eu.pb4.placeholders.api.Placeholders.DEFAULT_PLACEHOLDER_PARSER);
            parsers.add(eu.pb4.placeholders.api.parsers.MarkdownLiteParserV1.ALL);
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
            parsers.add(eu.pb4.placeholders.api.Placeholders.DEFAULT_PLACEHOLDER_PARSER);
            return NodeParser.merge(parsers);
            #elif MC_VER > MC_1_20_2
            return NodeParser
                .builder()
                .globalPlaceholders().build();
            #endif
        }

        private static int registerExtendedTags() {
            #if MC_VER <= MC_1_20_2
            eu.pb4.placeholders.api.parsers.TextParserV1.registerDefault(
                eu.pb4.placeholders.api.parsers.TextParserV1.TextTag.of(
                    "newline",
                    List.of("newline"),
                    "formatting",
                    true,
                    (tag, data, input, handlers, endAt) -> new eu.pb4.placeholders.api.parsers.TextParserV1.TagNodeValue(new LiteralNode("\n"), 0)
                )
            );

            #elif MC_VER > MC_1_20_2
            eu.pb4.placeholders.api.parsers.tag.TagRegistry.registerDefault(
                eu.pb4.placeholders.api.parsers.tag.TextTag.self(
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
                .replace("*", "\\*")
                .replace("%", "\\%")
                .replace("§", "&");
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
                // NOTE: Prevent the class cast exception in client side.
                if (!PlayerHelper.isServerPlayer(playerEntity)) {
                    LogUtil.debug("PlayerEntity {} is a client-side player entity, we can't use it to make the context for placeholder parser. (I will just fallback to the server context).");
                    return PlaceholderContext.of(ServerHelper.getServer());
                }

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
        public static final Map<String, String> PLAYER_2_LANGUAGE_CODE = new ConcurrentHashMap<>();
        public static final Map<String, JsonObject> LANGUAGE_CODE_2_LANGUAGE_JSON = new ConcurrentHashMap<>();
        private static final JsonObject UNSUPPORTED_LANGUAGE_MARKER = new JsonObject();

        private static void writeDefaultLanguageFilesIfAbsent() {
            for (String languageFileName : getLanguageFileNameGraph()) {
                String languageCode = LanguageConfigurationHandler.toLanguageCode(languageFileName);
                loadResourceConfigurationHandler(languageCode);
            }
        }

        private static @NotNull List<String> getLanguageFileNameGraph() {
            return ReflectionUtil.CompileTimeGraph.getCompileTimeGraph(ReflectionUtil.CompileTimeGraph.LANGUAGE_GRAPH_FILE_NAME);
        }

        @ForDeveloper("""
            Clear the loaded language file in memory.
            Note that once teh attempt to load a language file from storage is failed, a JsonObject maker named `UNSUPPORTED LANGUAGE` will be put into the map.
            Leading the subsequent attempts imply returns the marker.
            """)
        public static void clearLoadedLanguageJsons() {
            LANGUAGE_CODE_2_LANGUAGE_JSON.clear();
        }

        public static void setPlayerLanguageCode(String playerName, String languageRepresentationUsedByMojang) {
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
                ResourceConfigurationHandler languageConfigurationHandler = loadResourceConfigurationHandler(languageCode);
                JsonObject languageJsonObject = languageConfigurationHandler.model().getAsJsonObject();
                fixParserInput(languageJsonObject);
                LANGUAGE_CODE_2_LANGUAGE_JSON.put(languageCode, languageJsonObject);
                LogUtil.info("Language {} loaded.", languageCode);
            } catch (Exception e) {
                LANGUAGE_CODE_2_LANGUAGE_JSON.put(languageCode, UNSUPPORTED_LANGUAGE_MARKER);
                LogUtil.error("Failed to load language `{}` from storage.", languageCode, e);
            }

        }

        private static void fixParserInput(JsonObject jsonObject) {
            jsonObject
                .keySet()
                .forEach(key -> {
                    String originalValue = jsonObject.get(key).getAsString();
                    String newValue = Fixer.fixParserInput(originalValue);
                    jsonObject.addProperty(key, newValue);
                });
        }

        private static @NotNull ResourceConfigurationHandler loadResourceConfigurationHandler(@NotNull String languageCode) {
            LanguageConfigurationHandler languageFileHandler = new LanguageConfigurationHandler(languageCode);
            languageFileHandler.readStorage();
            return languageFileHandler;
        }

        @SuppressWarnings("StringSplitter")
        private static String unifyLanguageCode(String input) {
            if (input == null || !input.contains("_")) {
                return input;
            }

            String[] components = input.split("_");
            String language = StringUtil.toLowerCase(components[0]);
            String region = StringUtil.toUpperCase(components[1]);
            return language + "_" + region;
        }

        private static @NotNull String getAudienceLanguageCode(@Nullable Object audience) {
            // If audience is null, just use the default language.
            if (audience == null) {
                return getDefaultLanguageCode();
            }

            // If audience is non-null, but we have no clue what is the source player, then still use the default language.
            @Nullable PlayerEntity player = tryExtractPlayerEntity(audience);
            if (player == null) {
                return getDefaultLanguageCode();
            }

            // Get the language code used by the player.
            String playerName = PlayerHelper.getPlayerName(player);
            String defaultLanguageCode = getDefaultLanguageCode();
            return PLAYER_2_LANGUAGE_CODE.computeIfAbsent(playerName, k -> defaultLanguageCode);
        }

        @SuppressWarnings({"IfCanBeSwitch", "PatternVariableCanBeUsed"})
        private static @Nullable PlayerEntity tryExtractPlayerEntity(@NotNull Object audience) {
            PlayerEntity player = null;

            /* Unbox the command source from command context first. */
            if (audience instanceof CommandContext<?> commandContext) {
                audience = commandContext.getSource();
            }

            /* Try extract the player entity from the audience. */
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

        public static boolean isDefaultLanguageCode(String languageCode) {
            return languageCode.equals(getDefaultLanguageCode());
        }

        public static boolean shouldUseBuiltInDocStrings() {
            if (isUsingEnglishAsTheDefaultLanguage()) return true;
            if (Configs.MAIN_CONTROL_CONFIG.model().core.document.alwaysUseBuiltInDocStrings) return true;

            return false;
        }

        private static boolean isUsingEnglishAsTheDefaultLanguage() {
            return getDefaultLanguageCode().equalsIgnoreCase("en_US");
        }

        public static boolean isUnSupportedLanguageJsonObject(JsonObject languageJson) {
            return UNSUPPORTED_LANGUAGE_MARKER.equals(languageJson);
        }
    }

    @ForDeveloper("The functions to map language key into language value for specified language code.")
    public static class Translator {

        private static @Nullable String getLanguageValueFromLanguageJson(@NotNull String languageCode, @NotNull String languageKey) {
            /* Get the language json object. */
            JsonObject languageJson = Loader.getLanguageJsonObject(languageCode);

            /* If the client-side language is not supported in server side, fallback to specified default language. */
            String defaultLanguageCode = Loader.getDefaultLanguageCode();
            if (Loader.isUnSupportedLanguageJsonObject(languageJson)) {
                languageCode = defaultLanguageCode;
                languageJson = Loader.getLanguageJsonObject(languageCode);
            }

            /* Get the language value from language json object. */
            if (languageJson.has(languageKey)) {
                return languageJson.get(languageKey).getAsString();
            }

            // Use partial locale. (Fallback to default language for one missing key)
            // NOTE: Actually, we will add the missing keys when loading a language file.
            if (!Loader.isDefaultLanguageCode(languageCode)) {
                LogUtil.warn("There is no language key {} in language code {}. We will fallback to default language code {} for this key.", languageKey, languageCode, defaultLanguageCode);
                return getLanguageValueFromLanguageJson(defaultLanguageCode, languageKey);
            }

            // If the language key is missing in the default language, then we have nothing to do.
            LogUtil.error("Failed to load the language key {} in the default language code. (default language code = {})", languageKey, defaultLanguageCode);
            return null;
        }

        public static @NotNull String getLanguageValueByKey(@Nullable Object audience, @NotNull String languageKey) {
            /* Get the language value for that audience. */
            String languageCode = Loader.getAudienceLanguageCode(audience);
            String languageValue = getLanguageValueFromLanguageJson(languageCode, languageKey);
            if (languageValue != null) {
                return languageValue;
            }

            /* Return a dummy value for easier debugging. */
            String dummyLanguageValue = "<red>(No key `%s` in language `%s`)</red>".formatted(languageKey, languageCode);
            LogUtil.warn("Failed to get language value: language code = {}, language key = {}, audience = {}", languageCode, languageKey, audience);
            return dummyLanguageValue;
        }

    }

    @ForDeveloper("The functions to operate on the Text domain entity.")
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

        public static MutableText replaceTextWithNamedArgument(@NotNull Text text, @NotNull String namedArgumentName, @NotNull Function<Matcher, Text> replacementSupplier) {
            return replaceTextWithRegex(text, "\\$\\{%s}".formatted(namedArgumentName), replacementSupplier);
        }

        public static MutableText replaceTextWithPattern(@NotNull Text text, @NotNull Pattern pattern, @NotNull Function<Matcher, Text> nonMemorizedReplacementSupplier) {
            // memorize the supplier
            nonMemorizedReplacementSupplier = makeMemoizeSupplier(nonMemorizedReplacementSupplier);
            return replaceText(text, pattern, nonMemorizedReplacementSupplier);
        }

        public static MutableText replaceTextWithRegex(@NotNull Text text, @NotNull String regex, @NotNull Function<Matcher, Text> nonMemorizedReplacementSupplier) {
            return replaceTextWithPattern(text, Pattern.compile(regex), nonMemorizedReplacementSupplier);
        }

        private static MutableText replaceText(@NotNull Text text, @NotNull Pattern pattern, @NotNull Function<Matcher, Text> replacementSupplier) {
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

        private static @Nullable List<Text> trySplitString(String string, Pattern pattern, @NotNull Function<Matcher, Text> replacementSupplier) {
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
                Text replacementText = replacementSupplier.apply(matcher);
                ret.add(replacementText);

                // update the start index.
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

        private static <T> Function<Matcher, T> makeMemoizeSupplier(@NotNull Function<Matcher, T> delegate) {
            AtomicReference<T> value = new AtomicReference<>();
            return (matcher) -> {
                T val = value.get();
                if (val == null) {
                    val = value.updateAndGet(cur -> cur == null ?
                        Objects.requireNonNull(delegate.apply(matcher)) : cur);
                }
                return val;
            };
        }

        public static @NotNull MutableText condenseTextList(List<Text> textList) {
            MutableText condensedText = Text.empty();
            for (int i = 0; i < textList.size(); i++) {
                condensedText.append(textList.get(i));
                if (i != textList.size() - 1) {
                    condensedText.append(TEXT_NEWLINE);
                }
            }
            return condensedText;
        }
    }

    @ForDeveloper("This is the core method to map String into Text.")
    public static @NotNull Text getText(@NotNull NodeParser parser, @Nullable Object audience, boolean isKey, String keyOrValue, Object... args) {
        // Retrieve the language value.
        String languageValue = isKey ? Translator.getLanguageValueByKey(audience, keyOrValue) : keyOrValue;

        // Check NPE.
        if (languageValue == null) {
            LogUtil.warn("The language value is null: parser = {}, audience = {}, isKey = {}, keyOrValue = {}, args = {}", parser, audience, isKey, keyOrValue, args);
            return Text.literal("The language value is null, see the console.");
        }

        // Format string arguments.
        languageValue = formatStringArgs(languageValue, args);

        // Make placeholder context.
        PlaceholderContext placeholderContext = Parsers.makePlaceholderContext(audience);
        ParserContext parserContext = ParserContext.of(PlaceholderContext.KEY, placeholderContext);

        // Fix the parser input.
        languageValue = Fixer.fixParserInput(languageValue);

        // Call text parser to parse the string.
        return parser.parseText(TextNode.of(languageValue), parserContext);
    }

    @SuppressWarnings("AnnotateFormatMethod")
    private static @NotNull String formatStringArgs(@NotNull String string, Object... args) {
        // NOTE: This brings the in-consistent between Text Placeholder API. (e.g. `%player:name%` and `%%player:name%%`.)
        // NOTE: When `Java Standard Formatter` is used, you have to write `%%` to mean the `%` for `Text Placeholder API`.
        if (args.length > 0) {
            try {
                return String.format(string, args);
            } catch (Exception e) {
                // NOTE: The Java standard formatter will not throw any exception, if given arguments are too many.
                LogUtil.error("""
                    Failed to format arguments in language value.
                    The language value is `{}`.
                    The arguments are `{}`.

                    Possible reasons:
                    1. There may be a syntax mistake in the language file. (Try fix it, and issue `/fuji reload` to reload the language file)
                    2. You have to write `%%` to mean `%` inside the Java Standard String Formatter. (https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html)
                    3. Outdated language value. (The number of args is mis-matched, try delete the old language value, and issue `/fuji reload` to re-generate a new default value.)
                    """, string, args, e);
            }
        }

        // No args to format, return the original string directly.
        return string;
    }


    public static @NotNull Text getTextByKey(@Nullable Object audience, String languageKey, Object... args) {
        return getText(Parsers.POWERFUL_PARSER, audience, true, languageKey, args);
    }

    public static @NotNull Text getTextByValue(@Nullable Object audience, String languageValue, Object... args) {
        return getText(Parsers.POWERFUL_PARSER, audience, false, languageValue, args);
    }

    public static Text getTextByKeyAndReplaceTheKeyword(@Nullable Object audience, String languageKey, String keywordName) {
        /* Get the keyword value. */
        String keywordKey = "keyword." + keywordName;
        String keywordValue = Translator.getLanguageValueByKey(audience, keywordKey);

        /* Replace with the keyword value. */
        return getTextByKey(audience, languageKey, keywordValue);
    }

    private static @NotNull List<Text> getTextList(@Nullable Object audience, boolean isKey, String keyOrValue) {
        /* Get the language value string first. */
        String languageValue = isKey ? Translator.getLanguageValueByKey(audience, keyOrValue) : keyOrValue;

        /* Split the language value string into lines, and parse it into text list. */
        return Arrays
            .stream(splitStringIntoLines(languageValue))
            .map(line -> getTextByValue(audience, line))
            .toList();
    }

    private static String[] splitStringIntoLines(String string) {
        return string.split("\n|<newline>");
    }

    public static @NotNull List<Text> getTextListByKey(@Nullable Object audience, String key) {
        return getTextList(audience, true, key);
    }

    public static @NotNull List<Text> getTextListByValue(@Nullable Object audience, String value) {
        return getTextList(audience, false, value);
    }

    public static @NotNull List<Text> getTextListByValue(@Nullable Object audience, List<String> lines) {
        List<Text> textList = new ArrayList<>();
        lines.forEach(line -> {
            Text lineText = TextHelper.getTextByValue(audience, line);
            textList.add(lineText);
        });
        return textList;
    }

    @ForDeveloper("Use this function to send a text to an audience.")
    public static void sendTextByKey(@NotNull Object audience, String languageKey, Object... args) {
        /* Get the language value by language key for that audience. */
        String languageValue = Translator.getLanguageValueByKey(audience, languageKey);

        /* Process the instructions in language value. */
        if (languageValue.startsWith(Sender.SUPPRESS_SENDING_STRING_MARKER)) {
            LogUtil.debug("Suppress the sending of text: audience = {}, languageKey = {}, args = {}", audience, languageKey, args);
            return;
        }

        Pair<String, Sender.TextLocation> languageInstructionResult = Sender.parseLanguageInstruction(languageValue);
        languageValue = languageInstructionResult.getKey();
        Sender.TextLocation textLocation = languageInstructionResult.getValue();

        /* Parse the language value into text using parsers. */
        Text text = getTextByValue(audience, languageValue, args);

        /* Pick the way to send the text to the audience. */
        // Unbox the command context, to get the command source.
        if (audience instanceof CommandContext<?> ctx) {
            audience = ctx.getSource();
        }

        try {
            Sender.sendTextToAudience(audience, text, textLocation);
        } catch (Exception e) {
            Object audienceType = (audience == null ? null : audience.getClass().getName());

            LogUtil.error("""
                Failed to send a text to the audience {}.
                ◉ Audience Type: {}
                ◉ Language Key: {}
                ◉ Args: {}
                ◉ Language Value: {}
                """, audience, audienceType, languageKey, args, languageValue, e);
        }
    }

    @ForDeveloper("The functions to send a text to the audience.")
    public static class Sender {
        private static final String SUPPRESS_SENDING_STRING_MARKER = "[suppress-sending]";
        public static final String SEND_ACTION_BAR_MARKER = "[send-action-bar]";
        public static final String SEND_MAIN_TITLE_MARKER = "[send-main-title]";
        public static final String SEND_SUB_TITLE_MARKER = "[send-sub-title]";

        private static void sendMessageToServerPlayerEntity(ServerPlayerEntity serverPlayerEntity, Text text) {
            if (serverPlayerEntity.networkHandler == null) {
                LogUtil.warn("Failed to send the message to player {}, because its network handler is null. (Is it an dummy offline player entity?): text = {}", serverPlayerEntity, text);
                return;
            }
            serverPlayerEntity.sendMessage(text, false);
        }

        private static void sendActionBarToAudience(Object audience, Text text) {
            if (audience instanceof PlayerEntity playerEntity) {
                playerEntity.sendMessage(text, true);
                return;
            }

            LogUtil.warn("Don't know how to send an ACTION-BAR to audience {}. (text = {})", audience, text);
            throw new IllegalStateException();
        }

        private static void sendTitleToAudience(Object audience, Text text, boolean useMainTitle) {
            if (audience instanceof ServerPlayerEntity serverPlayerEntity) {
                if (useMainTitle) {
                    sendTitleToServerPlayerEntity(serverPlayerEntity, text, Text.empty());
                } else {
                    sendTitleToServerPlayerEntity(serverPlayerEntity, Text.empty(), text);
                }
                return;
            }

            LogUtil.warn("Don't know how to send a TITLE to audience {}. (text = {})", audience, text);
            throw new IllegalStateException();
        }

        private static void sendMessageToAudience(Object audience, Text text) {
            if (audience instanceof ServerPlayerEntity serverPlayerEntity) {
                sendMessageToServerPlayerEntity(serverPlayerEntity, text);
                return;
            }

            if (audience instanceof PlayerEntity playerEntity) {
                playerEntity.sendMessage(text, false);
                return;
            }

            if (audience instanceof ServerCommandSource serverCommandSource) {
                if (serverCommandSource.getPlayer() != null) {
                    ServerPlayerEntity player = serverCommandSource.getPlayer();
                    sendMessageToServerPlayerEntity(player, text);
                    return;
                }
                serverCommandSource.sendMessage(text);
                return;
            }

            LogUtil.warn("Don't know how to send a MESSAGE to audience {}. (text = {})", audience, text);
            throw new IllegalStateException();
        }

        private static void sendTitleToServerPlayerEntity(@NotNull ServerPlayerEntity player, @NotNull Text mainTitle, @NotNull Text subTitle) {
            sendTitleToServerPlayerEntity(player, 10, 70, 20, mainTitle, subTitle);
        }

        public static void sendTitleToServerPlayerEntity(ServerPlayerEntity player, int fadeInTicks, int stayTicks, int fadeOutTicks, Text mainTitle, Text subTitle) {
            player.networkHandler.sendPacket(new TitleFadeS2CPacket(fadeInTicks, stayTicks, fadeOutTicks));
            player.networkHandler.sendPacket(new TitleS2CPacket(mainTitle));
            player.networkHandler.sendPacket(new SubtitleS2CPacket(subTitle));
        }

        private static void sendTextToAudience(@NotNull Object audience, @NotNull Text text, @Nullable Sender.TextLocation textLocation) {
            if (TextLocation.ACTION_BAR == textLocation) {
                sendActionBarToAudience(audience, text);
            } else if (TextLocation.MAIN_TITLE == textLocation) {
                sendTitleToAudience(audience, text, true);
            } else if (TextLocation.SUB_TITLE == textLocation) {
                sendTitleToAudience(audience, text, false);
            } else {
                sendMessageToAudience(audience, text);
            }
        }

        private static Pair<String, TextLocation> parseLanguageInstruction(@NotNull String string) {
            TextLocation textLocation = TextLocation.MESSAGE;
            if (string.startsWith(SEND_ACTION_BAR_MARKER)) {
                string = string.substring(SEND_ACTION_BAR_MARKER.length());
                textLocation = TextLocation.ACTION_BAR;
            } else if (string.startsWith(SEND_MAIN_TITLE_MARKER)) {
                string = string.substring(SEND_MAIN_TITLE_MARKER.length());
                textLocation = TextLocation.MAIN_TITLE;
            } else if (string.startsWith(SEND_SUB_TITLE_MARKER)) {
                string = string.substring(SEND_SUB_TITLE_MARKER.length());
                textLocation = TextLocation.SUB_TITLE;
            }
            return new Pair<>(string, textLocation);
        }

        public enum TextLocation {
            MESSAGE, ACTION_BAR, MAIN_TITLE, SUB_TITLE
        }
    }

    public static void sendBroadcastByKey(@NotNull String key, Object... args) {
        /* Log the console, using the default language. */
        Text text = getTextByKey(null, key, args);
        LogUtil.info(Operators.visitString(text));

        /* Send the text using the player's client side language. */
        for (ServerPlayerEntity player : PlayerHelper.Lookup.getOnlinePlayers()) {
            TextHelper.sendTextByKey(player, key, args);
        }
    }

    public static void sendBroadcastByText(Text text) {
        /* Log the console, using the given text. */
        LogUtil.info(Operators.visitString(text));

        /* Send the text, using the given text. */
        for (ServerPlayerEntity player : PlayerHelper.Lookup.getOnlinePlayers()) {
            player.sendMessage(text);
        }
    }

    public static Text getDocumentText(Object audience, String docString) {
        docString = DocumentUtil.compileDocumentString(docString);
        return getText(Parsers.STYLE_ONLY_PARSER, audience, false, docString);
    }

    public static List<Text> getDocumentTextList(Object audience, String docString) {
        return Arrays
            .stream(splitStringIntoLines(docString))
            .map(line -> getDocumentText(audience, line))
            .toList();
    }

    @ForDeveloper("The abstraction for text events.")
    public static class Events {

        public static class ClickEvent {

            public static net.minecraft.text.ClickEvent makeSuggestCommandAction(String command) {
                #if MC_VER <= MC_1_21_4
                return new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.SUGGEST_COMMAND, command);
                #elif MC_VER >= MC_1_21_5
                return new net.minecraft.text.ClickEvent.SuggestCommand(command);
                #endif
            }

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

    public static class Formatter {

        public static <K, V> MutableText formatMapMultiLine(Map<K, V> map) {
            return formatMap(map, "", "", "\n");
        }

        public static <K, V> MutableText formatMapInLine(Map<K, V> map) {
            return formatMap(map, "{", "}", ", ");
        }

        private static <K, V> @NotNull MutableText formatMap(Map<K, V> map, String prefixString, String suffixString, String splitterString) {
            MutableText builder = Text.empty();

            boolean firstElement = true;
            builder.append(Text.literal(prefixString));
            for (Map.Entry<K, V> entry : map.entrySet()) {
                if (!firstElement) {
                    MutableText splitterText = Text
                        .literal(splitterString)
                        .fillStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY));
                    builder.append(splitterText);
                }
                firstElement = false;

                Text entryText = TextHelper.getTextByKey(null, "map.entry", entry.getKey(), entry.getValue());
                builder.append(entryText);
            }
            builder.append(Text.literal(suffixString));

            return builder;
        }

    }

    public static class Fixer {

        @TestCase(steps = "Issue `/fuji`, and see the `document of afk module`, the `details of run module` and the `details of skin` module.", purposes = {
            "The text parser should parse the text properly from the earliest version to the latest version."
            , "The URL highlighter should work properly."
            , "Ensure the `</>` doesn't break the style of texts."
        })
        public static String fixParserInput(String input) {
            // NOTE: Older Text Placeholder API can't parse the closing tag for custom color style tag. (e.g. `</#FF0000>`)
            #if MC_VER < MC_1_20_4
            return input.replaceAll("</#.+?>", "</>");
            #elif MC_VER >= MC_1_20_4
            return input;
            #endif
        }

    }

    @ForDeveloper("""
        The text component format: https://minecraft.wiki/w/Text_component_format#History
        """)
    @SuppressWarnings("unused")
    public static class Codec {

        @SuppressWarnings("unused")
        public static String toJson(@NotNull Text text) {
            #if MC_VER <= MC_1_20_2
            return Text.Serializer.toJson(text);
            #elif MC_VER > MC_1_20_2 && MC_VER <= MC_1_20_4
            return Text.Serialization.toJsonString(text);
            #elif MC_VER > MC_1_20_4
            return net.minecraft.text.TextCodecs.CODEC
                .encodeStart(JsonOps.INSTANCE, text)
                .getOrThrow()
                .toString();
            #endif
        }

        @SuppressWarnings("unused")
        public static Text fromJson(@NotNull String textJson) {
            #if MC_VER <= MC_1_20_2
            return Text.Serializer.fromJson(textJson);
            #elif MC_VER > MC_1_20_2 && MC_VER <= MC_1_20_4
            return Text.Serialization.fromJson(textJson);
            #elif MC_VER > MC_1_20_4
            return net.minecraft.text.TextCodecs.CODEC
                .decode(JsonOps.INSTANCE, JsonUtil.readJsonString(textJson))
                .getOrThrow()
                .getFirst();
            #endif
        }
    }

}


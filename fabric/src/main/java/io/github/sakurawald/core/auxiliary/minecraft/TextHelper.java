package io.github.sakurawald.core.auxiliary.minecraft;

import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.node.LiteralNode;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.tag.TagRegistry;
import eu.pb4.placeholders.api.parsers.tag.TextTag;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.core.config.Configs;
import io.github.sakurawald.core.config.handler.impl.ResourceConfigurationHandler;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class TextHelper {

    /* constants */
    public static final Text TEXT_NEWLINE = Text.of("\n");
    public static final Text TEXT_SPACE = Text.of(" ");

    /* class states */
    private static final NodeParser POWERFUL_PARSER = NodeParser.builder()
        .quickText()
        .simplifiedTextFormat()
        .globalPlaceholders()
        .markdown()
        .build();

    private static final NodeParser PLACEHOLDER_PARSER = NodeParser.builder()
        .globalPlaceholders().build();

    private static final Map<String, String> player2code = new HashMap<>();
    private static final Map<String, JsonObject> code2json = new HashMap<>();
    private static final JsonObject UNSUPPORTED_LANGUAGE_MARKER = new JsonObject();

    private static final String SUPPRESS_SENDING_STRING_MARKER = "[suppress-sending]";
    private static final Text SUPPRESS_SENDING_TEXT_MARKER = Text.literal("[suppress-sending]");

    static {
        writeDefaultLanguageFilesIfAbsent();

        TagRegistry.registerDefault(
            TextTag.self(
                "newline",
                "formatting",
                true,
                (nodes, data, parser) -> new LiteralNode("\n")
            )
        );

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
            LogUtil.info("language {} loaded.", languageCode);
        } catch (Exception e) {
            code2json.put(languageCode, UNSUPPORTED_LANGUAGE_MARKER);
            LogUtil.warn("failed to load language `{}`", languageCode);
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

        PlayerEntity player = switch (audience) {
            case ServerPlayerEntity serverPlayerEntity -> serverPlayerEntity;
            case PlayerEntity playerEntity -> playerEntity;
            case ServerCommandSource source when source.getPlayer() != null -> source.getPlayer();
            default -> null;
        };

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
        return convertToLanguageCode(Configs.configHandler.model().core.language.default_language);
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

    public static @NotNull String parsePlaceholder(@Nullable Object audience, String value) {
        return TextHelper.getText(PLACEHOLDER_PARSER, audience, false, value).getString();
    }

    /* This is the core method to map `String` into `Text`.
     *  All methods that return `Vomponent` are converted from this method.
     * */
    private static @NotNull Text getText(@NonNull NodeParser parser, @Nullable Object audience, boolean isKey, String keyOrValue, Object... args) {
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
        LogUtil.info(text.getString());

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
}

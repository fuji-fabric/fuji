package io.github.sakurawald.fuji.module.initializer.echo.send_custom;

import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.echo.send_custom.command.argument.wrapper.CustomTextName;
import io.github.sakurawald.fuji.core.service.paged_text.PagedBookText;
import io.github.sakurawald.fuji.core.service.paged_text.PagedMessageText;
import io.github.sakurawald.fuji.core.service.paged_text.PagedText;
import lombok.SneakyThrows;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Document(id = 1751976654799L, value = """
    This module provides `/send-custom` command.
    To define `custom text` and auto-page them.
    And then send it `as message` or `as book` to a specified player.
    """)
@ColorBox(id = 1753055756228L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Download the example custom text.
    You can download the `example-custom-text.txt` file.
    It is in https://github.com/sakurawald/fuji/blob/dev/.github/files/example-custom-text.txt
    """)
@ColorBox(id = 1751976733551L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Send a custom text as a book.
    Issue: `/send-custom as-book Alice guide --author "alice" --title "<rb>The Guide" --giveBook true --openBook true`

    ◉ Send a custom text as a message.
    Issue: `/send-custom as-message Alice guide`
    """)
@ColorBox(id = 1753331827763L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Ensure the players are allowed to use `/command-callback` command.
    The `/command-callback` command is a fuji command, used for `click event`.
    In vanilla Minecraft, if a player has `no permission` to use that command, the client will says `Unknown Command` error.
    """)



@CommandNode("send-custom")
@CommandRequirement(level = 4)
public class SendCustomInitializer extends ModuleInitializer {

    public static final Path CUSTOM_TEXT_DIR_PATH = ReflectionUtil.computeModuleConfigPath(SendCustomInitializer.class)
        .resolve("custom-text");

    private static String withCustomText(ServerPlayerEntity player, CustomTextName name) {
        String value = name.getValue();
        Path resolve = CUSTOM_TEXT_DIR_PATH.resolve(value);
        try {
            return Files.readString(resolve);
        } catch (IOException e) {
            TextHelper.sendTextByKey(player, "echo.send_custom.custom_text.not_found", value);
            throw new AbortCommandExecutionException();
        }
    }

    @Document(id = 1751826990344L, value = "Send the `custom text` as a `message`.")
    @CommandNode("as-message")
    private static int $asMessage(@CommandSource ServerCommandSource source, ServerPlayerEntity player, CustomTextName name) {
        String string = withCustomText(player, name);

        PagedMessageText pagedMessageText = new PagedMessageText(player, string);
        pagedMessageText.sendPage(player, 0);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826992414L, value = "Send the `custom text` as a `book`.")
    @CommandNode("as-book")
    private static int $asBook(@CommandSource ServerCommandSource source
        , ServerPlayerEntity player
        , CustomTextName customTextName
        , Optional<Boolean> openBook
        , Optional<Boolean> giveBook
        , Optional<String> title
        , Optional<String> author
    ) {
        String string = withCustomText(player, customTextName);
        /* make paged text */
        PagedText pagedText = new PagedBookText(player, string);

        /* make book element */
        BookElementBuilder bookElementBuilder = new BookElementBuilder();
        author.ifPresent(bookElementBuilder::setAuthor);
        title.ifPresent(it -> bookElementBuilder.setName(TextHelper.getTextByValue(player, it)));
        pagedText.getPages().forEach(bookElementBuilder::addPage);

        /* make the gui */
        BookGui gui = new BookGui(player, bookElementBuilder) {
            @Override
            public void onTakeBookButton() {
                this.close();
            }
        };

        if (giveBook.orElse(true)) {
            ItemStack copy = gui.getBook().copy();
            player.giveItemStack(copy);
        }

        if (openBook.orElse(true)) {
            gui.open();
        }

        return CommandHelper.Return.SUCCESS;
    }

    @SneakyThrows(IOException.class)
    @Override
    protected void onInitialize() {
        Files.createDirectories(CUSTOM_TEXT_DIR_PATH);
    }
}

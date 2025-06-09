package io.github.sakurawald.module.initializer.echo.send_custom;

import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import io.github.sakurawald.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.echo.send_custom.command.argument.wrapper.CustomTextName;
import io.github.sakurawald.module.initializer.echo.send_custom.structure.PagedBookText;
import io.github.sakurawald.module.initializer.echo.send_custom.structure.PagedMessageText;
import io.github.sakurawald.module.initializer.echo.send_custom.structure.PagedText;
import lombok.SneakyThrows;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

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
            TextHelper.sendMessageByKey(player, "echo.send_custom.custom_text.not_found", value);
            throw new AbortCommandExecutionException();
        }
    }

    @CommandNode("as-message")
    private static int asMessage(@CommandSource ServerCommandSource source, ServerPlayerEntity player, CustomTextName name) {
        String string = withCustomText(player, name);

        PagedMessageText pagedMessageText = new PagedMessageText(player, string);
        pagedMessageText.sendPage(player, 0);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("as-book")
    private static int asBook(@CommandSource ServerCommandSource source
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

    @SneakyThrows
    @Override
    protected void onInitialize() {
        Files.createDirectories(CUSTOM_TEXT_DIR_PATH);
    }
}

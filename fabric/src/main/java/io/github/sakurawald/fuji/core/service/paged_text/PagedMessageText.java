package io.github.sakurawald.fuji.core.service.paged_text;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.manager.Managers;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.text.Text;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;

public class PagedMessageText extends PagedText {

    public PagedMessageText(ServerPlayerEntity player, String string) {
        String[] split = string.split(NEW_PAGE_DELIMITER);
        this.pages = new ArrayList<>();
        Arrays.stream(split).forEach(it -> this.pages.add(TextHelper.getTextByValue(player, it)));
        generateClickCallbacks(player);
    }

    public PagedMessageText(ServerPlayerEntity player, List<Text> pages) {
        this.pages = pages;
        generateClickCallbacks(player);
    }

    public static @NotNull <T> PagedMessageText makePagedMessageText(ServerPlayerEntity player, List<T> entities, int pageSize, TriConsumer<T, Integer, MutableText> entityConsumer) {
        List<Text> pages = new ArrayList<>();
        MutableText pageBuilder = Text.empty();
        for (int i = 0; i < entities.size(); i++) {
            if ((i % pageSize == 0 && i != 0)) {
                pages.add(pageBuilder);
                pageBuilder = Text.empty();
            }

            T entity = entities.get(i);
            entityConsumer.accept(entity, i, pageBuilder);
            pageBuilder.append(TextHelper.TEXT_NEWLINE);

            if (i == entities.size() - 1) {
                pages.add(pageBuilder);
            }
        }

        return new PagedMessageText(player, pages);
    }

    private void generateClickCallbacks(ServerPlayerEntity player) {
        /* generate page callbacks */
        List<String> pageCallbacks = new ArrayList<>();
        for (int i = 0; i < getPages().size(); i++) {
            pageCallbacks.add(i, this.makeClickCallbackCommand(i));
        }

        /* single page message doesn't need the paginator */
        if (pageCallbacks.size() == 1) {
            return;
        }

        /* generate paginator */
        int totalPages = getPages().size();
        for (int i = 0; i < getPages().size(); i++) {
            MutableText text = getPages().get(i).copy();

            int currentPage = i + 1;
            /* make the paginator */
            if (i == 0) {
                text.append(TextHelper.getTextByKey(player, "echo.send_custom.custom_text.paginator.first_page", currentPage, totalPages, pageCallbacks.get(i + 1)));
            } else if (i == getPages().size() - 1) {
                text.append(TextHelper.getTextByKey(player, "echo.send_custom.custom_text.paginator.last_page", pageCallbacks.get(i - 1), currentPage, totalPages));
            } else {
                text.append(TextHelper.getTextByKey(player, "echo.send_custom.custom_text.paginator.middle_page", pageCallbacks.get(i - 1), currentPage, totalPages, pageCallbacks.get(i + 1)));
            }

            /* append the paginator */
            getPages().set(i, text);
        }
    }

    private String makeClickCallbackCommand(int pageIndex) {
        return Managers.getCallbackManager().makeCallbackCommand((player) -> {
            if (pageIndex < 0 || pageIndex >= this.getPages().size()) {
                TextHelper.sendTextByKey(player, "echo.send_custom.custom_text.invalid_page");
                return;
            }

            TextHelper.sendText(player, this.getPages().get(pageIndex));
        }, 1, TimeUnit.HOURS);
    }


    public void sendPage(ServerPlayerEntity player, int pageIndex) {
        TextHelper.sendText(player, this.getPages().get(pageIndex));
    }

}

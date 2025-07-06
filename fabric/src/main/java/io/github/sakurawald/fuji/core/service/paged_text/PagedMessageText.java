package io.github.sakurawald.fuji.core.service.paged_text;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.manager.Managers;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PagedMessageText extends PagedText {

    public PagedMessageText(ServerPlayerEntity player, String string) {
        /* split pages */
        String[] split = string.split(NEW_PAGE_DELIMITER);
        this.pages = new ArrayList<>();
        Arrays.stream(split).forEach(it -> pages.add(TextHelper.getTextByValue(player, it)));

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

            player.sendMessage(this.getPages().get(pageIndex));
        }, 1, TimeUnit.HOURS);
    }


    public void sendPage(ServerPlayerEntity player, int pageIndex) {
        player.sendMessage(getPages().get(pageIndex));
    }

}

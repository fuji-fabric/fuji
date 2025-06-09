package io.github.sakurawald.module.initializer.echo.send_custom.structure;

import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Arrays;

public class PagedBookText extends PagedText {

    public PagedBookText(ServerPlayerEntity player, String string) {
        String[] split = string.split(NEW_PAGE_DELIMITER);
        this.pages = new ArrayList<>();
        Arrays.stream(split).forEach(it -> pages.add(TextHelper.getTextByValue(player, it)));
    }
}

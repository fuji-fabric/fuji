package mod.fuji.core.service.paged_text;

import mod.fuji.core.auxiliary.minecraft.TextHelper;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Arrays;

public class PagedBookText extends PagedText {

    public PagedBookText(ServerPlayer player, String string) {
        String[] split = string.split(NEW_PAGE_DELIMITER);
        this.pages = new ArrayList<>();
        Arrays.stream(split).forEach(it -> pages.add(TextHelper.getTextByValue(player, it)));
    }
}

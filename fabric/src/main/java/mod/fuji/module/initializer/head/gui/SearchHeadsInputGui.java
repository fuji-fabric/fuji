package mod.fuji.module.initializer.head.gui;

import mod.fuji.core.auxiliary.StringUtil;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.InputSignGui;
import mod.fuji.module.initializer.head.privoder.HeadProvider;
import mod.fuji.module.initializer.head.structure.Head;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class SearchHeadsInputGui extends InputSignGui {

    private final @NotNull HeadGui parentGui;

    public SearchHeadsInputGui(@NotNull HeadGui parentGui) {
        super(parentGui.getPlayer(), null);
        this.parentGui = parentGui;
    }

    @Override
    public void onClose() {
        String keywords = joinStrings();

        /* If no user input, re-open the parent gui. */
        if (keywords.isBlank()) {
            parentGui.open();
            return;
        }

        /* Filter the entities by keywords. */
        List<Head> entities = HeadProvider.getLoadedHeads().values()
            .stream()
            .filter(head -> StringUtil.containsIgnoreCase(head.name, keywords)
            || StringUtil.containsIgnoreCase(head.getTagsOrEmpty(), keywords))
            .collect(Collectors.toList());

        Text title = TextHelper.getTextByKey(player, "gui.search.title", keywords);
        new CategoryHeadsGui(this.parentGui, player, title, entities, 0)
            .open();
    }
}

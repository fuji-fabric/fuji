package io.github.sakurawald.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.IOUtil;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.gui.PagedGui;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationHandlerGui extends PagedGui<BaseConfigurationHandler<?>> {

    public ConfigurationHandlerGui(@Nullable SimpleGui parent, ServerPlayerEntity player, @NotNull List<BaseConfigurationHandler<?>> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "fuji.inspect.configuration.gui.title"), entities, pageIndex);
    }

    @Override
    protected PagedGui<BaseConfigurationHandler<?>> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<BaseConfigurationHandler<?>> entities, int pageIndex) {
        return new ConfigurationHandlerGui(getGui(), player, entities, pageIndex);
    }

    @Override
    protected GuiElementInterface toGuiElement(BaseConfigurationHandler<?> entity) {
        String modelClassName = entity.getClass().getSimpleName();
        if (modelClassName.isBlank()) {
            modelClassName = "ANONYMOUS-CLASS";
        }
        Path modelPath = entity.getPath();
        String topLevelName = IOUtil.computeRelativePath(modelPath.toFile());

        return new GuiElementBuilder()
            .setItem(Items.BOOKSHELF)
            .setName(Text.literal(IOUtil.computeRelativePath(entity.getPath().toFile())))
            .setLore(List.of(
                TextHelper.getTextByKey(getPlayer(), "fuji.inspect.configuration.class", modelClassName)
                , TextHelper.getTextByKey(getPlayer(), "fuji.inspect.configuration.path", modelPath)
            ))
            .setCallback(new JavaObjectGui(getGui(), entity.model(), getPlayer(), new ArrayList<>(), 0, topLevelName, "")::open)
            .build();
    }

    @Override
    protected List<BaseConfigurationHandler<?>> filter(String keyword) {
        return getEntities().stream()
            .filter(it -> it.getClass().getSimpleName().contains(keyword)
                || it.getPath().toString().contains(keyword))
            .toList();
    }
}

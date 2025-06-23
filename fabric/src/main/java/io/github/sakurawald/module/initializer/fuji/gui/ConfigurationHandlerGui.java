package io.github.sakurawald.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.IOUtil;
import io.github.sakurawald.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.gui.PagedGui;
import io.github.sakurawald.core.manager.impl.module.ModuleManager;
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
        String configModelClassName = ReflectionUtil.getSimpleClassName(entity.getClass());
        Path configPath = entity.getPath();
        String topLevelName = IOUtil.computeRelativePath(configPath.toFile());

        String fromModule = ModuleManager.computeModulePathAsString(entity.model().getClass().getName());

        List<Text> lore = List.of(
            TextHelper.getTextByKey(getPlayer(), "from_module", fromModule)
            , TextHelper.getTextByKey(getPlayer(), "fuji.inspect.configuration.class", configModelClassName)
            , TextHelper.getTextByKey(getPlayer(), "fuji.inspect.configuration.path", configPath)
        );

        return new GuiElementBuilder()
            .setItem(Items.TRAPPED_CHEST)
            .setName(Text.literal(topLevelName))
            .setLore(lore)
            .setCallback(new JavaObjectGui(getGui(), entity.model(), getPlayer(), new ArrayList<>(), 0, topLevelName, ".")::open)
            .build();
    }

    @Override
    protected List<BaseConfigurationHandler<?>> filter(String keyword) {
        return getEntities().stream()
            .filter(it -> ReflectionUtil.getSimpleClassName(it.getClass()).contains(keyword)
                || it.getPath().toString().contains(keyword))
            .toList();
    }
}

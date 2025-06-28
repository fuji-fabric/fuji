package io.github.sakurawald.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.IOUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.config.Configs;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.gui.PagedGui;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ConfigurationsInspectionGui extends PagedGui<BaseConfigurationHandler<?>> {

    public ConfigurationsInspectionGui(@Nullable SimpleGui parent, ServerPlayerEntity player, @NotNull List<BaseConfigurationHandler<?>> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "fuji.inspect.configuration.gui.title"), entities, pageIndex);
    }

    public static ConfigurationsInspectionGui inspectAll(SimpleGui parent, ServerPlayerEntity player) {
        List<BaseConfigurationHandler<?>> entities = BaseConfigurationHandler.REGISTERED_CONFIGURATION_HANDLERS
            .stream()
            .filter(it -> it instanceof ObjectConfigurationHandler<?>)
            .sorted(Comparator.comparing(BaseConfigurationHandler::getPath))
            .toList();

        return new ConfigurationsInspectionGui(parent, player, entities, 0);
    }

    @Override
    protected PagedGui<BaseConfigurationHandler<?>> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<BaseConfigurationHandler<?>> entities, int pageIndex) {
        return new ConfigurationsInspectionGui(parent, player, entities, pageIndex);
    }

    @Override
    protected GuiElementInterface toGuiElement(BaseConfigurationHandler<?> entity) {
        String configModelClassName = ReflectionUtil.getSimpleClassName(entity.getClass());
        String configRelativePath = IOUtil.computeRelativePath(entity.getPath().toFile());
        String fromModule = entity.getFromModule();

        List<Text> lore = List.of(
            TextHelper.getTextByKey(getPlayer(), "from_module", fromModule)
            , TextHelper.getTextByKey(getPlayer(), "fuji.inspect.configuration.class", configModelClassName)
            , TextHelper.getTextByKey(getPlayer(), "fuji.inspect.configuration.path",  configRelativePath)
        );

        // NOTE: The parent may be different, due to the parent of ConfigurationsInspectionGui may be null or non-null (If it's created and open from ModuleDetailsInspectionGui).
        SimpleGui trueParentGui = this.getParent() != null ? this.getParent() : this.getBackendGui();

        GuiElementBuilder guiElementBuilder = new GuiElementBuilder()
            .setItem(toItem(entity))
            .setName(Text.literal(configRelativePath))
            .setLore(lore)
            .setCallback(() -> inspectWithJavaObjectInspector(trueParentGui, entity, configRelativePath));

        return guiElementBuilder
            .build();
    }

    private void inspectWithJavaObjectInspector(SimpleGui parent, BaseConfigurationHandler<?> entity, String topLevelName) {
        new JavaObjectInspectionGui(parent, entity.model(), getPlayer(), new ArrayList<>(), 0, topLevelName, ".")
            .open();
    }

    private static Item toItem(BaseConfigurationHandler<?> entity) {
        if (entity == Configs.mainControlConfig) {
            return Items.ENDER_CHEST;
        }

        return Items.TRAPPED_CHEST;
    }

    @Override
    protected boolean filterEntity(BaseConfigurationHandler<?> entity, String keyword) {
        return ReflectionUtil.getSimpleClassName(entity.getClass()).contains(keyword)
                || entity.getPath().toString().contains(keyword);
    }
}

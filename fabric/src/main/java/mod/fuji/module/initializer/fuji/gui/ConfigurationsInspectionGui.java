package mod.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.ReflectionUtil;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.config.Configs;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.document.auxiliary.DocumentUtil;
import mod.fuji.core.gui.component.gui.PagedGui;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@TestCase(action = "Inspect the configurations of `command_menu` module.", targets = "It should be able to inspect complex data structures.")
public class ConfigurationsInspectionGui extends PagedGui<BaseConfigurationHandler<?>> {

    public ConfigurationsInspectionGui(@Nullable SimpleGui parent, ServerPlayerEntity player, @NotNull List<BaseConfigurationHandler<?>> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "fuji.inspect.configuration.gui.title"), entities, pageIndex);
    }

    public static ConfigurationsInspectionGui inspectAll(SimpleGui parent, ServerPlayerEntity player) {
        List<BaseConfigurationHandler<?>> entities = BaseConfigurationHandler.getObjectConfigurationHandlers();
        return new ConfigurationsInspectionGui(parent, player, entities, 0);
    }

    @Override
    protected PagedGui<BaseConfigurationHandler<?>> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<BaseConfigurationHandler<?>> entities, int pageIndex) {
        return new ConfigurationsInspectionGui(parent, player, entities, pageIndex);
    }

    @SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor")
    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull BaseConfigurationHandler<?> entity) {
        Class<?> configHandlerClass = entity.getClass();
        String configHandlerClassName = ReflectionUtil.getSimpleClassName(configHandlerClass);
        String configRelativePath = entity.computeRelativePathBasedOnGameDir();
        String fromModule = entity.getSourceModule();

        List<Text> lore = new ArrayList<>();
        lore.addAll(List.of(
            TextHelper.getTextByKey(getPlayer(), "from_module", fromModule)
            , TextHelper.getTextByKey(getPlayer(), "fuji.inspect.configuration.class", configHandlerClassName)
            , TextHelper.getTextByKey(getPlayer(), "fuji.inspect.configuration.path",  configRelativePath)
            , TextHelper.getTextByKey(getPlayer(), "prompt.click.see_inside")
        ));

        /* Attach document. */
        Class<?> configModelClass = entity.model().getClass();
        DocumentUtil
            .getClassDocumentString(getPlayer(), configModelClass)
            .ifPresent(configModelClassDocumentString -> {
                lore.add(TextHelper.TEXT_EMPTY);
                lore.addAll(TextHelper.getDocumentTextList(getPlayer(), configModelClassDocumentString));
            });

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
        if (entity == Configs.MAIN_CONTROL_CONFIG) {
            return Items.ENDER_CHEST;
        }

        return Items.TRAPPED_CHEST;
    }

}

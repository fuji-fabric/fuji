package mod.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import java.util.List;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.core.document.inspector.FailedToInspectException;
import mod.fuji.core.document.inspector.InspectingObject;
import mod.fuji.core.document.inspector.JavaObjectInspector;
import mod.fuji.core.gui.structure.GuiElementIR;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaObjectInspectionGui extends PagedGui<InspectingObject> {

    final @NotNull String fileRelativePath;
    final @NotNull JavaObjectInspector inspector;

    public JavaObjectInspectionGui(@Nullable SimpleGui parent, @NotNull ServerPlayer player, @NotNull List<InspectingObject> entities, int pageIndex, @NotNull String fileRelativePath, @NotNull JavaObjectInspector inspector) {
        super(parent, player, TextHelper.getTextByKey(player, "object.gui.title", inspector.getWalkingPath()), entities, pageIndex);

        /* Pass the variables along the inspecting path. */
        this.fileRelativePath = fileRelativePath;
        this.inspector = inspector;

        /* Place footer. */
        GuiHelper.Placer.setSlotInLastLine(this, 4, GuiHelper.Button.makeHelpButton(player)
            .setItem(Items.OAK_SAPLING)
            .setLore(List.of(
                TextHelper.getTextByKey(player, "object.top_level", fileRelativePath),
                TextHelper.getTextByKey(player, "object.path", inspector.getWalkingPath()),
                TextHelper.getTextByKey(player, "object.type", inspector.getParentInspectingObject().getObjectTypeString())
            )));
    }

    @Override
    protected @NotNull PagedGui<InspectingObject> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<InspectingObject> entities, int pageIndex) {
        return new JavaObjectInspectionGui(parent, player, entities, pageIndex, this.fileRelativePath, this.inspector);
    }

    @Override
    protected @NotNull GuiElementIR toGuiElement(@NotNull InspectingObject entity) {
        GuiElementBuilder guiElementBuilder = new GuiElementBuilder()
            .setName(entity.toNameText(getPlayer()))
            .setItem(GuiHelper.Material.fromObjectType(entity.getObjectValue(), entity.getObjectType()))
            .setLore(entity.toLore(getPlayer()))
            .setCallback(() -> openChildInspectorGui(entity));

        return GuiElementIR.of(guiElementBuilder.build());
    }

    private void openChildInspectorGui(@NotNull InspectingObject inspectingObject) {
        try {
            /* Make a new inspector for that non-atom object. */
            JavaObjectInspector childInspector = this.inspector.withChild(inspectingObject);

            /* Make a deeper GUI and open it. */
            new JavaObjectInspectionGui(getBackendGui(), getPlayer(), childInspector.getChildInspectingObjects(), 0, this.fileRelativePath, childInspector)
                .open();
        } catch (FailedToInspectException ignore) {
            // Can not open child inspector for target object.
        }
    }

}

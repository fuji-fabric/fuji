package mod.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import java.util.List;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.module.initializer.fuji.structure.FailedToInspectException;
import mod.fuji.module.initializer.fuji.structure.InspectingObject;
import mod.fuji.module.initializer.fuji.structure.JavaObjectInspector;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaObjectInspectionGui extends PagedGui<InspectingObject> {

    final @NotNull String fileRelativePath;
    final @NotNull JavaObjectInspector inspector;

    public JavaObjectInspectionGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull List<InspectingObject> entities, int pageIndex, @NotNull String fileRelativePath, @NotNull JavaObjectInspector inspector) {
        super(parent, player, TextHelper.getTextByKey(player, "object.gui.title", inspector.getWalkingPath()), entities, pageIndex);

        /* Pass the variables along the inspecting path. */
        this.fileRelativePath = fileRelativePath;
        this.inspector = inspector;

        /* Place footer. */
        GuiHelper.Placer.setSlotInLastLine(this, 4, GuiHelper.Button.makeHelpButton(player)
            .setLore(List.of(
                TextHelper.getTextByKey(player, "object.top_level", fileRelativePath)
            )));
    }

    @Override
    protected PagedGui<InspectingObject> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<InspectingObject> entities, int pageIndex) {
        return new JavaObjectInspectionGui(parent, player, entities, pageIndex, this.fileRelativePath, this.inspector);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull InspectingObject entity) {
        GuiElementBuilder guiElementBuilder = new GuiElementBuilder()
            .setName(entity.toNameText(getPlayer()))
            .setItem(GuiHelper.Material.fromObjectType(entity.getObjectValue(), entity.getObjectType()))
            .setLore(entity.toLore(getPlayer()))
            .setCallback(() -> openChildInspectorGui(entity));

        return guiElementBuilder.build();
    }

    private void openChildInspectorGui(@NotNull InspectingObject inspectingObject) {
        try {
            /* Make a new inspector for that non-atom object. */
            JavaObjectInspector childInspector = this.inspector.withChild(inspectingObject);

            /* Make a deeper GUI and open it. */
            new JavaObjectInspectionGui(getBackendGui(), getPlayer(), childInspector.getInspectingObjects(), 0, this.fileRelativePath, childInspector)
                .open();
        } catch (FailedToInspectException ignore) {
            // Can not open child inspector for target object.
        }
    }

}

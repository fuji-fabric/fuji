package io.github.sakurawald.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.StringUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.component.gui.PagedGui;
import io.github.sakurawald.fuji.module.initializer.fuji.structure.InspectingObject;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JavaObjectInspectionGui extends PagedGui<InspectingObject> {

    private final String fileRelativePath;
    private final String walkingPath;

    public JavaObjectInspectionGui(@Nullable SimpleGui parent, @Nullable Object objectToInspect, ServerPlayerEntity player, @NotNull List<InspectingObject> entities, int pageIndex, String fileRelativePath, @NotNull String walkingPath) {
        super(parent, player, TextHelper.getTextByKey(player, "object.gui.title", walkingPath), entities, pageIndex);

        /* Pass the variables along the inspecting path. */
        this.fileRelativePath = fileRelativePath;
        this.walkingPath = walkingPath;

        /* Inspect the Java object instance by demand. */
        if (objectToInspect != null) {
            this.getEntities().addAll(InspectingObject.inspectJavaObject(objectToInspect));
        }

        /* Place footer. */
        getFooter().setSlot(4, GuiHelper.Button.makeHelpButton(player)
            .setLore(List.of(
                TextHelper.getTextByKey(player, "object.top_level", fileRelativePath)
            )));
    }

    @Override
    protected PagedGui<InspectingObject> make(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<InspectingObject> entities, int pageIndex) {
        return new JavaObjectInspectionGui(parent, null, player, entities, pageIndex, this.fileRelativePath, this.walkingPath);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull InspectingObject entity) {
        GuiElementBuilder guiElementBuilder = new GuiElementBuilder()
            .setName(entity.computeNameText(getPlayer()))
            .setItem(entity.computeItem())
            .setLore(entity.computeLore(getPlayer()))
            .setCallback(() -> onClickToGoInside(entity));

        return guiElementBuilder.build();
    }

    private void onClickToGoInside(InspectingObject entity) {
        /* Define variables. */
        Object objectToInspect = entity.getObjectValue();

        // NOTE: The value of user-defined object may be `null` in some case.
        if (objectToInspect == null) {
            return;
        }

        String objectName = entity.getObjectName();

        /* We can't go inside an atom. */
        if (!entity.canGoInside()) return;

        /* Let's go deeper. */
        List<InspectingObject> newEntities = new ArrayList<>();

        /* Special case for Iterable, Map and Map.Entry types.  */
        if (Iterable.class.isAssignableFrom(objectToInspect.getClass())) {
            newEntities = ((Collection<?>) objectToInspect)
                .stream()
                .map(element -> new InspectingObject(element, null, "ELT"))
                .toList();

            objectToInspect = null;
        } else if (Map.class.isAssignableFrom(objectToInspect.getClass())) {
            newEntities = ((Map<?, ?>) objectToInspect)
                .entrySet()
                .stream()
                .map(entry -> {
                    // NOTE: For json, you can only have `string type` key.
                    String jsonObjectKeyName = null;
                    if (String.class.isAssignableFrom(entry.getKey().getClass())) {
                        jsonObjectKeyName = "\"" + entry.getKey() + "\"";
                    }

                    return new InspectingObject(entry, null, jsonObjectKeyName);
                })
                .toList();

            objectToInspect = null;
        } else if (Map.Entry.class.isAssignableFrom(objectToInspect.getClass())) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) objectToInspect;
            InspectingObject entryKeyObject = new InspectingObject(entry.getKey(), null, "KEY");
            InspectingObject entryValueObject = new InspectingObject(entry.getValue(), null, "VALUE");
            newEntities = List.of(entryKeyObject, entryValueObject);

            objectToInspect = null;
        }

        /* Compute the new walking path. */
        String newWalkingPath = this.walkingPath + "." + objectName;
        newWalkingPath = StringUtil.trimPathString(newWalkingPath);

        /* Make the deeper GUI and open it. */
        new JavaObjectInspectionGui(getBackendGui(), objectToInspect, getPlayer(), newEntities, 0, this.fileRelativePath, newWalkingPath)
            .open();
    }

}

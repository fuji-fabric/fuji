package mod.fuji.core.gui.structure;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor(staticName = "of")
@Getter
@EqualsAndHashCode
public class GuiElementIR {

    @Getter
    @Nullable("The getSlot() method may return a null value.")
    #if MC_VER < MC_26_1
        eu.pb4.sgui.api.elements.GuiElementInterface
    #elif MC_VER >= MC_26_1
        eu.pb4.sgui.api.elements.GuiElement
    #endif nativeValue;

}

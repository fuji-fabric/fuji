package io.github.sakurawald.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.abst.BaseEventConsumer;
import io.github.sakurawald.fuji.core.gui.component.gui.PagedGui;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import java.util.Collection;
import java.util.List;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EventsInspectionGui extends PagedGui<BaseEventConsumer<?>> {

    public EventsInspectionGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull List<BaseEventConsumer<?>> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "event.inspection.gui.title"), entities, pageIndex);
    }

    @Override
    protected PagedGui<BaseEventConsumer<?>> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<BaseEventConsumer<?>> entities, int pageIndex) {
        return new EventsInspectionGui(parent, player, entities, pageIndex);
    }

    public static EventsInspectionGui inspectAll(@NotNull ServerPlayerEntity player) {
        List<BaseEventConsumer<?>> entities = EventManager.events.values()
            .stream()
            .flatMap(Collection::stream)
            .toList();
        return new EventsInspectionGui(null, player, entities, 0);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull BaseEventConsumer<?> entity) {
        GuiElementBuilder builder = new GuiElementBuilder();
        String declaringClassName = entity.getCompiledEventConsumerMethod().getClass().getName();

        builder
            .setItem(Items.OBSERVER)
            .setName(TextHelper.getTextByKey(player, "event"))
            .setLore(List.of(
                TextHelper.getTextByKey(player, "from_module", ModuleManager.computeJoinedModulePath(declaringClassName)),
                TextHelper.getTextByKey(player, "event.type", entity.getEventType().getName()),
                TextHelper.getTextByKey(player, "event.consumer.name", entity.getEventConsumerInfo().getDeclaringMethodName()),
                TextHelper.getTextByKey(player, "event.consumer.lambda.name", declaringClassName),
                TextHelper.getTextByKey(player, "event.priority.injector", entity.getEventConsumerInfo().getInjectorPriority()),
                TextHelper.getTextByKey(player, "event.priority.consumer", entity.getEventConsumerInfo().getConsumerPriority())
            ));

        return builder.build();
    }

}

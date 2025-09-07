package io.github.sakurawald.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.consumer.BaseEventConsumer;
import io.github.sakurawald.fuji.core.gui.component.gui.PagedGui;
import io.github.sakurawald.fuji.core.manager.impl.module.ModulePathResolver;
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
        List<BaseEventConsumer<?>> entities = EventManager.getEvents().values()
            .stream()
            .flatMap(Collection::stream)
            .toList();
        return new EventsInspectionGui(null, player, entities, 0);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull BaseEventConsumer<?> entity) {
        GuiElementBuilder builder = new GuiElementBuilder();
        String eventConsumerDeclaringClassName = entity.getEventConsumerMethod().getClass().getName();

        builder
            .setItem(Items.OBSERVER)
            .setName(TextHelper.getTextByKey(player, "event"))
            .setLore(List.of(
                TextHelper.getTextByKey(player, "from_module", ModulePathResolver.computeModulePathString(eventConsumerDeclaringClassName)),
                TextHelper.getTextByKey(player, "event.type", entity.getEventType().getName()),
                TextHelper.getTextByKey(player, "event.consumer.class", entity.getClass().getName()),
                TextHelper.getTextByKey(player, "event.consumer.declaring_class", eventConsumerDeclaringClassName),
                TextHelper.getTextByKey(player, "event.consumer.declaring_method", entity.getEventConsumerInfo().getDeclaringMethodName()),
                TextHelper.getTextByKey(player, "event.priority.injector", entity.getEventConsumerInfo().getInjectorPriority()),
                TextHelper.getTextByKey(player, "event.priority.consumer", entity.getEventConsumerInfo().getConsumerPriority())
            ));

        return builder.build();
    }

}

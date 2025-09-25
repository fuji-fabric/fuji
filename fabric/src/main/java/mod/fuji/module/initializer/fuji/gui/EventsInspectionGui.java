package mod.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.consumer.EventConsumer;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.core.manager.impl.module.ModulePathResolver;
import java.util.Collection;
import java.util.List;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EventsInspectionGui extends PagedGui<EventConsumer<?>> {

    public EventsInspectionGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull List<EventConsumer<?>> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "event.inspection.gui.title"), entities, pageIndex);
    }

    @Override
    protected PagedGui<EventConsumer<?>> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<EventConsumer<?>> entities, int pageIndex) {
        return new EventsInspectionGui(parent, player, entities, pageIndex);
    }

    public static EventsInspectionGui inspectAll(@NotNull ServerPlayerEntity player) {
        List<EventConsumer<?>> entities = EventManager.getEvents().values()
            .stream()
            .flatMap(Collection::stream)
            .toList();
        return new EventsInspectionGui(null, player, entities, 0);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull EventConsumer<?> entity) {
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

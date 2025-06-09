package io.github.sakurawald.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.gui.PagedGui;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JavaObjectGui extends PagedGui<Field> {

    private final String topLevel;
    private final String path;
    private final Object instance;

    public JavaObjectGui(@Nullable SimpleGui parent, Object instance, ServerPlayerEntity player, @NotNull List<Field> entities, int pageIndex, String topLevel, @NotNull String path) {
        super(parent, player, TextHelper.getTextByKey(player, "object.gui.title", path), entities, pageIndex);
        this.instance = instance;
        this.topLevel = topLevel;
        this.path = path;

        /* add entities if empty */
        if (this.getEntities().isEmpty()) {
            this.getEntities().addAll(JavaObjectGui.inspectObject(this.instance));
        }

        /* add footer */
        getFooter().setSlot(4, GuiHelper.makeHelpButton(player)
            .setLore(List.of(
                TextHelper.getTextByKey(player, "object.top_level", topLevel)
            )));
    }

    @Override
    protected PagedGui<Field> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<Field> entities, int pageIndex) {
        return new JavaObjectGui(getParent(), instance, player, entities, pageIndex, topLevel, path);
    }

    private Item computeItem(Field field) {
        return isAtom(field) ? Items.PAPER : Items.BOOK;
    }

    private Text computeName(Field field) {
        return Text.of(field.getName());
    }

    private List<Text> computeLore(Field field) {
        /* get value */
        Object value;
        try {
            field.setAccessible(true);
            value = field.get(instance);

            /* transform into size if it's a collection */
            if (value instanceof Collection<?> collection) {
                value = collection.size() + " entries";
            } else if (value instanceof Map<?, ?> map) {
                value = map.size() + " entries";
            }

        } catch (Exception e) {
            value = "FAILED-TO-ACCESS";
        }

        /* make lore */
        Text valueText = TextHelper.getTextByKey(getPlayer(), "object.value");
        String abbreviate = StringUtils.abbreviate(value.toString(), "...", 128);
        valueText = TextHelper.replaceTextWithMarker(valueText, "value", ()->Text.literal(abbreviate));

        return List.of(
            TextHelper.getTextByKey(getPlayer(), "object.type", field.getType().getSimpleName())
            , valueText);
    }

    @SuppressWarnings("RedundantIfStatement")
    private static boolean isAtom(Field field) {
        /* unbox the type */
        Class<?> type = field.getType();

        /* is atom? */
        if (type.isPrimitive()) return true;
        if (ReflectionUtil.isWrapperType(type)) return true;
        if (type.equals(String.class)) return true;

        if (type.isArray()) return true;
        if (type.isEnum()) return true;
        if (type.isAnnotation()) return true;

        if (Iterable.class.isAssignableFrom(type)) return true;
        if (Map.class.isAssignableFrom(type)) return true;

        return false;
    }

    private static List<Field> inspectObject(@NotNull Object instance) {
        return Arrays.stream(instance.getClass().getDeclaredFields())
            .sorted(Comparator.comparing(JavaObjectGui::isAtom))
            .collect(Collectors.toList());
    }

    @Override
    protected GuiElementInterface toGuiElement(Field entity) {
        return new GuiElementBuilder()
            .setName(computeName(entity))
            .setItem(computeItem(entity))
            .setLore(computeLore(entity))
            .setCallback(() -> {
                if (isAtom(entity)) return;

                try {
                    Object newInstance = entity.get(instance);
                    String newPath = StringUtils.strip(this.path + "." + entity.getName(), ".");
                    new JavaObjectGui(getGui(), newInstance, getPlayer(), new ArrayList<>(), 0, topLevel, newPath).open();
                } catch (IllegalAccessException e) {
                    LogUtil.error("failed to access the value of field {}", entity, e);
                }

            })
            .build();
    }

    @Override
    protected List<Field> filter(String keyword) {
        return getEntities().stream()
            .filter(it -> it.getName().contains(keyword))
            .collect(Collectors.toList());
    }

}

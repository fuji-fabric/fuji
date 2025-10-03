package mod.fuji.module.initializer.fuji.structure;

import mod.fuji.core.auxiliary.ReflectionUtil;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.document.auxiliary.DocumentUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class InspectingObject {

    public final Object object;
    public final @Nullable Object instanceOfField;
    public final @Nullable String preferredObjectName;

    public InspectingObject(Object object, @Nullable Object instanceOfField, @Nullable String preferredObjectName) {
        this.object = object;
        this.instanceOfField = instanceOfField;
        this.preferredObjectName = preferredObjectName;
    }

    public Class<?> getObjectType() {
        // NOTE: The implementation of this.getObjectValue().getClass(); didn't work. (Due to null value)

        /* Get the field type. */
        if (object instanceof Field field) {
            return field.getType();
        }

        return this.object.getClass();
    }

    private String getObjectValueReadableString() {
        Object value = this.getObjectValue();

        /* Optimize the readable string for collection types. */
        if (value instanceof Collection<?> collection) {
            return "%d elements".formatted(collection.size());
        }
        if (value instanceof Map<?, ?> map) {
            return "%d elements".formatted(map.size());
        }

        if (value instanceof Map.Entry<?, ?> entry) {
            String keyTypeString = entry.getKey().getClass().getSimpleName();
            String valueTypeString = entry.getValue().getClass().getSimpleName();
            return "mapper %s -> %s".formatted(keyTypeString, valueTypeString);
        }

        /* Call the implementation of toString() of the object. */
        // NOTE: value.toString() may throw NPE.
        return String.valueOf(value);
    }

    public Optional<String> getDocumentString(ServerPlayerEntity player) {
        /* Extract @Document from a field in class. */
        if (this.object instanceof Field field) {
            return DocumentUtil.getFieldDocumentString(player, field);
        }

        /* Extract @Document from collection and map. */
        Class<?> objectType = this.getObjectType();
        return DocumentUtil.getClassDocumentString(player, objectType);
    }

    public Object getObjectValue() {
        // NOTE: Be careful of the implicit call to toString() function. Here we should return the object value directly, to prevent NPE.

        /* Get the field value. */
        if (object instanceof Field field) {
            Object value;
            try {
                field.setAccessible(true);
                value = field.get(this.instanceOfField);
            } catch (Exception e) {
                value = "FAILED-TO-ACCESS";
            }

            return value;
        }

        return this.object;
    }


    private boolean isFieldType() {
        return this.object instanceof Field;
    }

    public String getObjectName() {
        /* If preferred object name is specified, simply return it. */
        if (this.preferredObjectName != null) {
            return this.preferredObjectName;
        }

        /* Compute the object name. */
        String objectName;
        if (this.isFieldType()) {
            objectName = ((Field) this.object).getName();
        } else {
            objectName = this.object.getClass().getSimpleName();
        }

        /* Decorate the object name if inspecting object is not a field. (It's an element in collection) */
        if (!this.isFieldType()) {
            objectName = "$" + objectName;
        }

        return objectName;
    }

    public Text computeNameText(ServerPlayerEntity player) {
        String objectName = this.getObjectName();
        objectName = TextHelper.Parsers.escapeTags(objectName);
        return TextHelper.getTextByKey(player, "object.name", objectName);
    }


    public static List<InspectingObject> inspectJavaObject(@NotNull Object object) {
        /* Ensure the object is unboxed value. */
        if (object instanceof InspectingObject) {
            object = ((InspectingObject) object).object;
        }

        /* Inspect the structure of object. */
        Object fieldInstance = object;
        Class<?> inspectingObjectClass = object.getClass();
        return ReflectionUtil.Reflection.gatherDeclaredFields(inspectingObjectClass)
            .stream()
            .filter(field -> {
                /* Ignore some fields that is not interested. */
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers)) return false;
                if (Modifier.isTransient(modifiers)) return false;
                return true;
            })
            .map(it -> new InspectingObject(it, fieldInstance, null))
            .toList();
    }

    public Item computeItem() {
        Class<?> type = this.getObjectType();

        if (Map.class.isAssignableFrom(type)) return Items.MAP;
        if (Iterable.class.isAssignableFrom(type)) return getIronChainItem();

        if (Boolean.class.isAssignableFrom(type)
            || boolean.class.isAssignableFrom(type)) {
            /* If the type of field is boolean, try to get its value. */
            Boolean booleanValue = (Boolean) this.getObjectValue();
            return booleanValue ? Items.GREEN_BANNER : Items.RED_BANNER;
        }

        if (String.class.isAssignableFrom(type)
            || Character.class.isAssignableFrom(type)
            || char.class.isAssignableFrom(type)) {
            return Items.STRING;
        }

        if (Byte.class.isAssignableFrom(type)
            || byte.class.isAssignableFrom(type)
            || Short.class.isAssignableFrom(type)
            || short.class.isAssignableFrom(type)
            || Integer.class.isAssignableFrom(type)
            || int.class.isAssignableFrom(type)
            || Long.class.isAssignableFrom(type)
            || long.class.isAssignableFrom(type)) {
            return Items.REDSTONE;
        }
        if (Float.class.isAssignableFrom(type)
            || float.class.isAssignableFrom(type)
            || Double.class.isAssignableFrom(type)
            || double.class.isAssignableFrom(type)) {
            return Items.GLOWSTONE_DUST;
        }

        if (Enum.class.isAssignableFrom(type)) {
            return Items.REPEATER;
        }

        return Items.PINK_SHULKER_BOX;
    }

    private static @NotNull Item getIronChainItem() {
        #if MC_VER < MC_1_21_9
        return Items.CHAIN;
        #elif MC_VER >= MC_1_21_9
        return Items.IRON_CHAIN;
        #endif
    }

    private void addPossibleValuesForEnumType(ServerPlayerEntity player, List<Text> lore) {
        if (!this.getObjectType().isEnum()) return;

        String possibleValues = ReflectionUtil.getEnumValuesCompactString(this.getObjectType());
        lore.add(TextHelper.getTextByKey(player, "object.value.possible_values", possibleValues));
    }

    public List<Text> computeLore(ServerPlayerEntity player) {
        List<Text> lore = new ArrayList<>();

        /* Add object type text. */
        String objectTypeString = getObjectTypeString();
        lore.add(TextHelper.getTextByKey(player, "object.type", objectTypeString));

        /* Add object value text. */
        String literalObjectValueString = getObjectValueString(true);
        lore.add(TextHelper.getText(TextHelper.Parsers.STYLE_ONLY_PARSER, player, true, "object.value", literalObjectValueString));

        /* Add possible enum values. */
        addPossibleValuesForEnumType(player, lore);

        /* Add click prompt. */
        if (ReflectionUtil.canInspectInside(this.getObjectType())) {
            lore.add(TextHelper.getTextByKey(player, "prompt.click.see_inside"));
        }

        /* Add @Document text. */
        this.getDocumentString(player)
            .ifPresent(documentString -> {
                lore.add(TextHelper.TEXT_EMPTY);
                lore.addAll(TextHelper.getDocumentTextList(player, documentString));
            });

        return lore;
    }

    public @NotNull String getObjectValueString(boolean abbreviated) {
        String literalObjectValueString = getObjectValueReadableString();
        if (abbreviated) {
            literalObjectValueString = StringUtils.abbreviate(literalObjectValueString, "...", 128);
        }
        literalObjectValueString = TextHelper.Parsers.escapeTags(literalObjectValueString);
        return literalObjectValueString;
    }

    private @NotNull String getObjectTypeString() {
        return this.getObjectType().getName();
    }

}

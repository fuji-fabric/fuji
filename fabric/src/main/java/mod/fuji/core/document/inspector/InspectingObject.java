package mod.fuji.core.document.inspector;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.ReflectionUtil;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.document.auxiliary.DocumentUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InspectingObject {

    /**
     * The <code>object</code> value may be <code>null</code> if the inspecting target is a <code>leaf node</code>.
     */
    final @NotNull Optional<Object> object;

    /**
     * If the inspecting <code>object</code> is type of <code>Field</code>, then this field is the instance of the class who declares that field.
     * Note that this field will be empty for any object that is not type of Field.
     */
    final @NotNull Optional<Object> objectDeclaringClassInstance;

    final @NotNull Optional<String> preferredObjectName;

    private InspectingObject(@NotNull Optional<Object> object, @NotNull Optional<Object> objectDeclaringClassInstance, @NotNull Optional<String> preferredObjectName) {
        this.object = object;
        this.objectDeclaringClassInstance = objectDeclaringClassInstance;
        this.preferredObjectName = preferredObjectName;
    }

    public static @NotNull InspectingObject ofRoot(@NotNull Object javaObject) {
        if (ReflectionUtil.isMetaClass(javaObject.getClass())) {
            throw new IllegalArgumentException("Cannot inspect object of type " + javaObject.getClass());
        }
        return ofNonFieldObject(Optional.of(javaObject), Optional.empty());
    }

    private static @NotNull InspectingObject ofNonFieldObject(@NotNull Optional<Object> javaObject, @NotNull Optional<String> preferredObjectName) {
        return new InspectingObject(javaObject, Optional.empty(), preferredObjectName);
    }

    private static @NotNull InspectingObject ofFieldObject(@NotNull Optional<Object> javaObject, @NotNull Optional<Object> objectDeclaringClassInstance, @NotNull Optional<String> preferredObjectName) {
        return new InspectingObject(javaObject, objectDeclaringClassInstance, preferredObjectName);
    }

    public @NotNull Class<?> getObjectType() {
        /* Get the field type. */
        return this.object
            .map($object -> {
                if ($object instanceof Field field) {
                    return field.getType();
                }

                return $object.getClass();
            })
            // NOTE: Return a sensible class type for `null` value.
            .orElse(Void.class);
    }

    public @NotNull Optional<Object> getObjectValue() {
        return object
            .map($object -> {
                /* Get the field value. */
                if ($object instanceof Field field) {
                    return this.objectDeclaringClassInstance
                        .map($objectDeclaringClassInstance -> {
                            try {
                                field.setAccessible(true);
                                // NOTE: The `null` value is a legal Json Value.
                                @Nullable Object fieldValue = field.get($objectDeclaringClassInstance);
                                return fieldValue;
                            } catch (Exception e) {
                                LogUtil.error("Failed to get the value of field {} in its declaring class instance {}.", field, $objectDeclaringClassInstance, e);
                                return null;
                            }
                        })
                        // Pass the `null` value. (Since any exception can lead to the null value, we can't distinguish which case we are in.)
                        .orElse(null);
                }

                /* Get the value of the object itself. */
                // NOTE: The value of user-defined object may be `null` if it's a leaf node.
                return $object;
            });
    }

    @SuppressWarnings("IfCanBeSwitch")
    private @NotNull String getObjectValueString() {
        /* Compute the value string. */
        @NotNull String valueString = this
            .getObjectValue()
            .map($objectValue -> {
                /* Optimize the readability of the string for various types. */
                if ($objectValue instanceof Collection<?> collection) {
                    return "%d elements".formatted(collection.size());
                }

                if ($objectValue instanceof Map<?, ?> map) {
                    return "%d mappings".formatted(map.size());
                }

                if ($objectValue instanceof Map.Entry<?, ?> entry) {
                    String keyTypeString = entry.getKey().getClass().getSimpleName();
                    String valueTypeString = entry.getValue().getClass().getSimpleName();
                    return "mapper %s -> %s".formatted(keyTypeString, valueTypeString);
                }

                /* Call the implementation of toString() of the object. */
                // NOTE: value.toString() may throw NPE.
                return String.valueOf($objectValue);
            })
            .orElse("null");

        /* Escape the string. */
        valueString = TextHelper.Parsers.escapeTags(valueString);
        return valueString;
    }


    public @NotNull String getObjectName() {
        /* If preferred object name is specified, simply return it. */
        return this
            .preferredObjectName
            .orElseGet(() -> {
                /* Compute the object name. */
                return this.object
                    .map($object -> {
                        if ($object instanceof Field field) {
                            return field.getName();
                        } else {
                            /* Decorate the object name if inspecting object is not a field. (It's an element in collection) */
                            return "$" + $object.getClass().getSimpleName();
                        }
                    })
                    .orElse("null");
            });
    }

    @SuppressWarnings("RedundantIfStatement")
    public static @NotNull List<InspectingObject> inspect(@NotNull InspectingObject inspectingObject) throws FailedToInspectException {
        return inspectingObject
            .getObjectValue()
            .map(objectToInspect -> {
                /* An atom can't be inspected. */
                if (!canInspect(inspectingObject.getObjectType())) {
                    throw new FailedToInspectException("Target object is considered as an atom.");
                }

                /* Handle special cases.  */
                if (Iterable.class.isAssignableFrom(objectToInspect.getClass())) {
                    // Special case: Iterable
                    Iterator<?> iterator = ((Iterable<?>) objectToInspect).iterator();
                    List<InspectingObject> result = new ArrayList<>();
                    for (int i = 0; iterator.hasNext(); i++) {
                        Object element = iterator.next();
                        String elementIndex = "[" + i + "]";
                        result.add(InspectingObject.ofNonFieldObject(Optional.ofNullable(element), Optional.of(elementIndex)));
                    }
                    return result;
                } else if (Map.class.isAssignableFrom(objectToInspect.getClass())) {
                    // Special case: Map
                    return ((Map<?, ?>) objectToInspect)
                        .entrySet()
                        .stream()
                        .map(entry -> {
                            // NOTE: For Json, you can only have `string type` key.
                            String jsonObjectKeyName = null;
                            if (String.class.isAssignableFrom(entry.getKey().getClass())) {
                                jsonObjectKeyName = "\"" + entry.getKey() + "\"";
                            }

                            return InspectingObject.ofNonFieldObject(Optional.of(entry), Optional.ofNullable(jsonObjectKeyName));
                        })
                        .toList();
                } else if (Map.Entry.class.isAssignableFrom(objectToInspect.getClass())) {
                    // Special case: Map.Entry
                    Map.Entry<?, ?> entry = (Map.Entry<?, ?>) objectToInspect;
                    InspectingObject entryKeyObject = InspectingObject.ofNonFieldObject(Optional.ofNullable(entry.getKey()), Optional.of("KEY"));
                    InspectingObject entryValueObject = InspectingObject.ofNonFieldObject(Optional.ofNullable(entry.getValue()), Optional.of("VALUE"));
                    return List.of(entryKeyObject, entryValueObject);
                }

                /* Inspect the user-defined object. */
                Class<?> inspectingObjectClass = objectToInspect.getClass();
                return ReflectionUtil.Reflection.gatherDeclaredFields(inspectingObjectClass)
                    .stream()
                    .filter(field -> {
                        /* Ignore some fields that is not interested. */
                        int modifiers = field.getModifiers();
                        if (Modifier.isStatic(modifiers)) return false;
                        if (Modifier.isTransient(modifiers)) return false;
                        return true;
                    })
                    .map(it -> InspectingObject.ofFieldObject(Optional.of(it), Optional.of(objectToInspect), Optional.empty()))
                    .toList();
            })
            .orElseThrow(() -> new FailedToInspectException("Target object is null"));
    }

    @SuppressWarnings("RedundantIfStatement")
    private static boolean canInspect(@NotNull Class<?> objectType) {
        /* Treat the following types as atom. */
        if (objectType.isPrimitive()) return false;
        if (ReflectionUtil.isPrimitiveWrapperType(objectType)) return false;

        if (objectType.equals(String.class)) return false;
        if (objectType.isArray()) return false;
        if (objectType.isEnum()) return false;
        if (objectType.isAnnotation()) return false;
        if (ReflectionUtil.isMetaClass(objectType)) return false;

        /* Treat other else types as non-atom. (Including Iterable and Map) */
        return true;
    }

    public @NotNull Component toNameText(@NotNull ServerPlayer player) {
        @NotNull String objectName = this.getObjectName();
        objectName = TextHelper.Parsers.escapeTags(objectName);
        return TextHelper.getTextByKey(player, "object.name", objectName);
    }

    private void addPossibleValuesForEnumType(@NotNull ServerPlayer player, @NotNull List<Component> lore) {
        if (!this.getObjectType().isEnum()) return;

        @NotNull String possibleValues = ReflectionUtil.getEnumValuesCompactString(this.getObjectType());
        lore.add(TextHelper.getTextByKey(player, "object.value.possible_values", possibleValues));
    }

    public @NotNull List<Component> toLore(@NotNull ServerPlayer player) {
        List<Component> lore = new ArrayList<>();

        /* Add object type text. */
        String objectTypeString = getObjectTypeString();
        lore.add(TextHelper.getTextByKey(player, "object.type", objectTypeString));

        /* Add object value text. */
        String objectValueString = getAbbreviatedObjectValueString();
        lore.add(TextHelper.getText(TextHelper.Parsers.STYLE_ONLY_PARSER, player, true, "object.value", objectValueString));

        /* Add possible enum values. */
        addPossibleValuesForEnumType(player, lore);

        /* Add click prompt. */
        if (canInspect(this.getObjectType())) {
            lore.add(TextHelper.getTextByKey(player, "prompt.click.see_inside"));
        }

        /* Add @Document text. */
        DocumentUtil.getAboveElementDocumentString(this.object, this.getObjectType(), player)
            .ifPresent(documentString -> {
                lore.add(TextHelper.TEXT_EMPTY);
                lore.addAll(TextHelper.getDocumentTextList(player, documentString));
            });

        return lore;
    }

    public @NotNull String getAbbreviatedObjectValueString() {
        /* Abbreviate the string if needed. */
        String valueString = getObjectValueString();
        valueString = StringUtils.abbreviate(valueString, "...", 128);
        return valueString;
    }

    public @NotNull String getObjectTypeString() {
        return this.getObjectType().getName();
    }

}

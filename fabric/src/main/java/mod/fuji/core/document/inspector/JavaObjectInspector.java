package mod.fuji.core.document.inspector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import mod.fuji.core.auxiliary.StringUtil;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
@AllArgsConstructor
@Getter
public class JavaObjectInspector {

    private static final String WALKING_PATH_DELIMITER = ".";
    final Optional<JavaObjectInspector> parent;
    final @NotNull String walkingPath;

    final @NotNull InspectingObject parentInspectingObject;
    final @NotNull List<InspectingObject> childInspectingObjects;

    public static @NotNull JavaObjectInspector ofRoot(@NotNull Object object) throws FailedToInspectException {
        @NotNull InspectingObject root = InspectingObject.ofRoot(object);
        @NotNull List<InspectingObject> children = InspectingObject.inspect(root);
        return new JavaObjectInspector(Optional.empty(), WALKING_PATH_DELIMITER, root, children);
    }

    public @NotNull JavaObjectInspector withChild(@NotNull InspectingObject inspectingObject) throws FailedToInspectException {
        /* Compute the grand children.*/
        List<InspectingObject> childInspectingObjects = InspectingObject.inspect(inspectingObject);

        /* Compute the parent inspector. */
        Optional<JavaObjectInspector> parentInspector = Optional.of(this);

        /* Compute the new walking path. */
        String childObjectName = inspectingObject.getObjectName();
        String childWalkingPath = this.getWalkingPath() + WALKING_PATH_DELIMITER + childObjectName;
        childWalkingPath = StringUtil.trimPathString(childWalkingPath);

        /* Return the child inspector. */
        return new JavaObjectInspector(parentInspector, childWalkingPath, inspectingObject, childInspectingObjects);
    }

    public @NotNull List<InspectingObject> flatten() {
        ArrayList<InspectingObject> collector = new ArrayList<>();
        flattenRecursively(collector, this.getParentInspectingObject());
        return collector;
    }

    private void flattenRecursively(@NotNull List<InspectingObject> collector, @NotNull InspectingObject parentInspectingObject) {
        /* Go down. */
        InspectingObject
            .inspect(parentInspectingObject)
            .forEach(it -> {
                try {
                    flattenRecursively(collector, it);
                } catch (FailedToInspectException ignore) {
                    // Continue...
                }
            });

        /* Go up. */
        collector.add(parentInspectingObject);
    }

}

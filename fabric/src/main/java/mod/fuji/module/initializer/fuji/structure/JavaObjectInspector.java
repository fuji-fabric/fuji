package mod.fuji.module.initializer.fuji.structure;

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

    final Optional<JavaObjectInspector> parent;
    final @NotNull String walkingPath;
    final @NotNull List<InspectingObject> inspectingObjects;

    public static @NotNull JavaObjectInspector ofRoot(@NotNull Object object) throws FailedToInspectException {
        @NotNull InspectingObject root = InspectingObject.ofRoot(object);
        @NotNull List<InspectingObject> children = InspectingObject.inspect(root);
        return new JavaObjectInspector(Optional.empty(), ".", children);
    }

    public @NotNull JavaObjectInspector withChild(@NotNull InspectingObject inspectingObject) throws FailedToInspectException {
        /* Compute the grand children.*/
        List<InspectingObject> childInspectingObjects = InspectingObject.inspect(inspectingObject);

        /* Compute the parent inspector. */
        Optional<JavaObjectInspector> parentInspector = Optional.of(this);

        /* Compute the new walking path. */
        String childObjectName = inspectingObject.getObjectName();
        String childWalkingPath = this.getWalkingPath() + "." + childObjectName;
        childWalkingPath = StringUtil.trimPathString(childWalkingPath);

        /* Return the child inspector. */
        return new JavaObjectInspector(parentInspector, childWalkingPath, childInspectingObjects);
    }

}

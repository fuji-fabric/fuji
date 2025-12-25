package mod.fuji.core.structure;

import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor(staticName = "of")
@Getter
@EqualsAndHashCode
public class IdentifierIR implements Comparable<IdentifierIR> {

    @Getter
    @NotNull
    #if MC_VER < MC_1_21_11
    net.minecraft.resources.ResourceLocation
    #elif MC_VER >= MC_1_21_11
    net.minecraft.resources.Identifier
    #endif nativeValue;

    @Override
    public @NotNull String toString() {
        return this.getNativeValue().toString();
    }

    public @NotNull String getNamespace() {
        return this.getNativeValue().getNamespace();
    }

    public @NotNull String getPath() {
        return this.getNativeValue().getPath();
    }

    public static @NotNull IdentifierIR makeIdentifierOrThrow(@NotNull String identifier) {
        #if MC_VER <= MC_1_20_6
        var result = IdentifierIR.of(new net.minecraft.resources.ResourceLocation(identifier));
        #elif MC_VER > MC_1_20_6 && MC_VER < MC_1_21_11
        var result = IdentifierIR.of(net.minecraft.resources.ResourceLocation.parse(identifier));
        #elif MC_VER >= MC_1_21_11
        var result = IdentifierIR.of(net.minecraft.resources.Identifier.parse(identifier));
        #endif

        Objects.requireNonNull(result, "Can't use %s to make a non-null Identifier object.".formatted(identifier));
        return result;
    }

    public static @NotNull IdentifierIR makeIdentifierOrThrow(@NotNull String namespace, @NotNull String path) {
        String identifier = namespace + ":" + path;
        return makeIdentifierOrThrow(identifier);
    }

    public static Optional<IdentifierIR> makeIdentifier(@NotNull String identifier) {
        try {
            return Optional.of(makeIdentifierOrThrow(identifier));
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    @Override
    public int compareTo(@NotNull IdentifierIR o) {
        return this.getNativeValue().compareTo(o.getNativeValue());
    }

}

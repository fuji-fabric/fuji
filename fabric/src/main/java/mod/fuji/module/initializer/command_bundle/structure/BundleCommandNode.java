package mod.fuji.module.initializer.command_bundle.structure;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.command.structure.CommandRequirementDescriptor;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BundleCommandNode {

    boolean enable = true;

    @Nullable
    String document;

    CommandRequirementDescriptor requirement;

    @SerializedName(value = "head", alternate = "pattern")
    String head;

    @SerializedName(value = "body", alternate = "bundle")
    List<String> body;
}

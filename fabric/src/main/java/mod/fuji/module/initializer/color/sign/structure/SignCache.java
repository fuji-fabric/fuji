package mod.fuji.module.initializer.color.sign.structure;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class SignCache {
    @SerializedName(value = "front_lines", alternate = "lines")
    List<String> frontLines = new ArrayList<>();

    List<String> backLines = new ArrayList<>();
}

package mod.fuji.module.initializer.title.config.model;

import mod.fuji.module.initializer.title.structure.TitleDescriptor;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TitleConfigModel {

    String defaultActiveTitleId = "resident";
    String noActiveTitleText = "<grey>[None]";

    List<TitleDescriptor> titleDescriptors = new ArrayList<>() {
        {
            TitleDescriptor residentTitle = new TitleDescriptor();
            residentTitle.setId("resident");
            residentTitle.setItem("minecraft:grass_block");
            residentTitle.setDisplayName("<dark_green>[Resident]");
            residentTitle.setLore(List.of("<yellow>The title for a resident."));
            this.add(residentTitle);

            TitleDescriptor farmerTitle = new TitleDescriptor();
            farmerTitle.setId("farmer");
            farmerTitle.setItem("minecraft:iron_hoe");
            farmerTitle.setDisplayName("<green>[Farmer]");
            farmerTitle.setLore(List.of("<yellow>The title for a farmer."));
            this.add(farmerTitle);

            TitleDescriptor fisherTitle = new TitleDescriptor();
            fisherTitle.setId("fisher");
            fisherTitle.setItem("minecraft:fishing_rod");
            fisherTitle.setDisplayName("<blue>[Fisher]");
            fisherTitle.setLore(List.of("<yellow>The title for a fisher."));
            this.add(fisherTitle);
        }
    };

}

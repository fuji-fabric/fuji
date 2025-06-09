package io.github.sakurawald.module.initializer.echo.send_custom.structure;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.text.Text;

import java.util.List;

@Data
@NoArgsConstructor
public class PagedText {

    protected static final String NEW_PAGE_DELIMITER = "<newpage>";

    protected List<Text> pages;

}

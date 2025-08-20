package io.github.sakurawald.fuji.module.initializer.skin.structure;

import com.mojang.authlib.properties.Property;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkinDescriptor {

    @Document(id = 1753249433410L, value = """
        A name used to describe how this skin looks like.
        """)
    String skinName;

    @Document(id = 1753249413432L, value = """
        The `properties` of this `skin`.
        """)
    PropertyWrapper skinProperty;

}

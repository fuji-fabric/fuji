package mod.fuji.core.document.annotation;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD})
@Repeatable(value = ColorBoxes.class)
public @interface ColorBox {

    long id();

    ColorBoxTypes color();

    String value();

    enum ColorBoxTypes {
        EXAMPLE("colorbox.example.name", Items.LIGHT_GRAY_CANDLE)
        , TIP("colorbox.tips.name", Items.LIME_CANDLE)
        , NOTE("colorbox.note.name", Items.BLUE_CANDLE)
        , WARNING("colorbox.warning.name", Items.YELLOW_CANDLE)
        , DANGER("colorbox.danger.name", Items.RED_CANDLE);

        final String languageKey;
        @SuppressWarnings("ImmutableEnumChecker")
        final Item item;

        ColorBoxTypes(String languageKey, Item item) {
            this.languageKey = languageKey;
            this.item = item;
        }

        public String toLanguageKey() {
            return this.languageKey;
        }

        public Item toItem() {
            return this.item;
        }

    }
}

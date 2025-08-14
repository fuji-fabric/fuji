package io.github.sakurawald.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.component.gui.PagedGui;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AboutGui extends PagedGui<Person> {

    public AboutGui(SimpleGui parent, ServerPlayerEntity player, @NotNull List<Person> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "about"), entities, pageIndex);

        ModMetadata metadata = FabricLoader.getInstance().getModContainer(Fuji.MOD_ID).get().getMetadata();
        getFooter().setSlot(1, new GuiElementBuilder(Items.BOOK)
            .setName(TextHelper.getTextByKey(player, "version.format", metadata.getVersion().getFriendlyString())));
        getFooter().setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(TextHelper.getTextByKey(player, "homepage.project"))
            .setCallback(() -> {
                TextHelper.sendTextByKey(player, "homepage.project.visit", metadata.getContact().get("sources").orElse("can't read project homepage from metadata"));
                this.closeWithoutOpenParentGui();
            }));
    }

    public static AboutGui make(@Nullable SimpleGui parent, ServerPlayerEntity player) {
        ModMetadata metadata = FabricLoader.getInstance().getModContainer(Fuji.MOD_ID)
            .orElseThrow(() -> new IllegalStateException("Failed to get the metadata of this mod."))
            .getMetadata();

        List<Person> persons = new ArrayList<>();
        persons.addAll(metadata.getAuthors());
        persons.addAll(metadata.getContributors());

        return new AboutGui(parent, player, persons, 0);
    }

    @Override
    protected void drawPagedGui() {
        super.drawPagedGui();

        GuiHelper.PlayerHead.fetchPlayerHeadTextures(this, this::draw);
    }

    @Override
    protected PagedGui<Person> make(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<Person> entities, int pageIndex) {
        return new AboutGui(parent, player, entities, pageIndex);
    }

    @SuppressWarnings("HttpUrlsUsage")
    private boolean isUrl(String string) {
        return string.contains("http://") || string.contains("https://");
    }

    public GuiElementInterface.ClickCallback makeCallback(Person entity) {
        return (a, b, c, d) -> {
            // construct the text
            MutableText text = Text.empty();
            text.append(TextHelper.getTextByKey(getPlayer(), "contact.visit.name", entity.getName()))
                .append(TextHelper.TEXT_NEWLINE);
            entity.getContact().asMap().forEach((k, v) -> text
                .append(TextHelper.getTextByKey(getPlayer(), isUrl(v) ? "contact.visit.entry.is_url" : "contact.visit.entry.is_not_url", k, v, v))
                .append(TextHelper.TEXT_NEWLINE));

            // send it
            TextHelper.sendText(getPlayer(), text);
            this.closeWithoutOpenParentGui();
        };
    }

    public List<Text> makeTextListFromContact(ContactInformation contact) {
        List<Text> ret = new ArrayList<>();
        contact.asMap().forEach((k, v) -> ret.add(TextHelper.getTextByKey(getPlayer(), "contact.entry", k, v)));

        // add visit hint lore
        ret.add(TextHelper.getTextByKey(getPlayer(), "contact.click.prompt"));
        return ret;
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull Person entity) {
        return GuiHelper.Button.makeLuckyBlockButton()
            .setName(TextHelper.getTextByKey(getPlayer(), "contact.name", entity.getName()))
            .setLore(makeTextListFromContact(entity.getContact()))
            .setCallback(makeCallback(entity))
            .build();
    }

}

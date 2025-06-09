package io.github.sakurawald.module.initializer.fuji.gui;

import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.Fuji;
import io.github.sakurawald.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.gui.PagedGui;
import io.github.sakurawald.core.service.gameprofile_fetcher.MojangProfileFetcher;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AboutGui extends PagedGui<Person> {

    public AboutGui(ServerPlayerEntity player, @NotNull List<Person> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "about"), entities, pageIndex);

        ModMetadata metadata = FabricLoader.getInstance().getModContainer(Fuji.MOD_ID).get().getMetadata();
        getFooter().setSlot(1, new GuiElementBuilder(Items.BOOK)
            .setName(TextHelper.getTextByKey(player, "version.format", metadata.getVersion().getFriendlyString())));
        getFooter().setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(TextHelper.getTextByKey(player, "homepage.project"))
            .setCallback(() -> {
                TextHelper.sendMessageByKey(player, "homepage.project.visit", metadata.getContact().get("sources").orElse("can't read project homepage from metadata"));
                this.close();
            }));
    }

    @Override
    protected void drawPagedGui() {
        super.drawPagedGui();

        // fetch heads async
        fetchHeads();
    }

    private void fetchHeads() {
        for (int i = 0; i < 54; i++) {
            GuiElementInterface slot = this.getSlot(i);
            if (slot == null) return;

            /* run async for each head */
            int finalI = i;
            CompletableFuture.runAsync(() -> {
                ItemStack itemStack = slot.getItemStack();

                // get the player name from the item name
                String onlinePlayerName = itemStack.getName().getString().trim();
                GameProfile gameProfile = MojangProfileFetcher.makeOnlineGameProfile(onlinePlayerName);

                // with gui slot
                GuiElementBuilder builder = new GuiElementBuilder()
                    .setItem(itemStack.getItem())
                    .setName(itemStack.getName())
                    .setCallback(slot.getGuiCallback())
                    .setSkullOwner(gameProfile, ServerHelper.getServer());

                LoreComponent loreComponent = itemStack.get(DataComponentTypes.LORE);
                if (loreComponent != null) {
                    builder.setLore(loreComponent.comp_2400());
                }

                setSlot(finalI, builder);

                // draw it
                draw();
            });
        }
    }

    @Override
    protected PagedGui<Person> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<Person> entities, int pageIndex) {
        return new AboutGui(player, entities, pageIndex);
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
            getPlayer().sendMessage(text);
            close();
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
    protected GuiElementInterface toGuiElement(Person entity) {
        return GuiHelper.makeUnknownPlayerSkull()
            .setName(TextHelper.getTextByKey(getPlayer(), "contact.name", entity.getName()))
            .setLore(makeTextListFromContact(entity.getContact()))
            .setCallback(makeCallback(entity))
            .build();
    }

    @Override
    protected List<Person> filter(String keyword) {
        return getEntities().stream().filter(e -> {
                Map<String, String> contact = e.getContact().asMap();
                return e.getName().contains(keyword)
                    || contact.entrySet().stream().anyMatch(it -> it.getKey().contains(keyword) || it.getValue().contains(keyword));
            }
        ).toList();
    }
}

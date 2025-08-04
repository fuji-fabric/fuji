package io.github.sakurawald.fuji.module.initializer.command_attachment.config.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.attachment_entry.BlockCommandAttachmentEntry;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.attachment_entry.BaseCommandAttachmentEntry;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.CommandAttackmentType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.attachment_entry.EntityCommandAttachmentEntry;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.attachment_entry.ItemStackCommandAttachmentEntry;
import java.lang.reflect.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandAttachmentNodeAdapter implements JsonDeserializer<BaseCommandAttachmentEntry> {

    @Override
    public @Nullable BaseCommandAttachmentEntry deserialize(@NotNull JsonElement json, Type typeOfT, @NotNull JsonDeserializationContext context) throws JsonParseException {
        /* Use ItemStack type if there is no a type field. */
        if (!json.getAsJsonObject().has("type")) {
            // treat as item stack command attachment entry if type is null.
            json.getAsJsonObject().addProperty("type", CommandAttackmentType.ITEMSTACK.name());
        }

        /* Dispatch the deserializer by the type field. */
        String type = json.getAsJsonObject().get("type").getAsString();
        if (type.equals(CommandAttackmentType.ITEMSTACK.name()))
            return context.deserialize(json, ItemStackCommandAttachmentEntry.class);
        if (type.equals(CommandAttackmentType.ENTITY.name()))
            return context.deserialize(json, EntityCommandAttachmentEntry.class);
        if (type.equals(CommandAttackmentType.BLOCK.name()))
            return context.deserialize(json, BlockCommandAttachmentEntry.class);

        LogUtil.error("Don't know how to de-serialize the command attachment entry: json = {}", json);
        throw new IllegalArgumentException("The type of command attachment entry is not supported!");
    }

}

package mod.fuji.module.initializer.command_attachment.config.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.config.interfaces.ObjectTypeStringGetter;
import mod.fuji.module.initializer.command_attachment.structure.attachment_entry.BlockCommandAttachmentEntry;
import mod.fuji.module.initializer.command_attachment.structure.attachment_entry.BaseCommandAttachmentEntry;
import mod.fuji.module.initializer.command_attachment.structure.attachment_entry.CommandAttackmentType;
import mod.fuji.module.initializer.command_attachment.structure.attachment_entry.EntityCommandAttachmentEntry;
import mod.fuji.module.initializer.command_attachment.structure.attachment_entry.ItemStackCommandAttachmentEntry;
import java.lang.reflect.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandAttachmentEntryAdapter implements JsonDeserializer<BaseCommandAttachmentEntry> {

    @Override
    public @Nullable BaseCommandAttachmentEntry deserialize(@NotNull JsonElement json, Type typeOfT, @NotNull JsonDeserializationContext context) throws JsonParseException {
        /* Use ItemStack type if there is no a type field. */
        if (!json.getAsJsonObject().has(ObjectTypeStringGetter.TYPE_KEY)) {
            // NOTE: Treat as item stack command attachment entry if type is null.
            json.getAsJsonObject().addProperty(ObjectTypeStringGetter.TYPE_KEY, CommandAttackmentType.ITEMSTACK.name());
        }

        /* Dispatch the deserializer by the type field. */
        String type = json.getAsJsonObject().get(ObjectTypeStringGetter.TYPE_KEY).getAsString();
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

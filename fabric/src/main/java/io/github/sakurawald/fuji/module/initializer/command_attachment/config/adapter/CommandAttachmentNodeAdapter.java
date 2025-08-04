package io.github.sakurawald.fuji.module.initializer.command_attachment.config.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.BlockCommandAttachmentNode;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.CommandAttachmentNode;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.CommandAttackmentType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.EntityCommandAttachmentNode;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.ItemStackCommandAttachmentNode;
import java.lang.reflect.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandAttachmentNodeAdapter implements JsonDeserializer<CommandAttachmentNode> {

    @Override
    public @Nullable CommandAttachmentNode deserialize(@NotNull JsonElement json, Type typeOfT, @NotNull JsonDeserializationContext context) throws JsonParseException {
        if (!json.getAsJsonObject().has("type")) {
            // treat as item stack command attachment entry if type is null.
            json.getAsJsonObject().addProperty("type", CommandAttackmentType.ITEMSTACK.name());
        }

        String type = json.getAsJsonObject().get("type").getAsString();
        if (type.equals(CommandAttackmentType.ITEMSTACK.name()))
            return context.deserialize(json, ItemStackCommandAttachmentNode.class);
        if (type.equals(CommandAttackmentType.ENTITY.name()))
            return context.deserialize(json, EntityCommandAttachmentNode.class);
        if (type.equals(CommandAttackmentType.BLOCK.name()))
            return context.deserialize(json, BlockCommandAttachmentNode.class);

        throw new IllegalArgumentException("The type of command attachment entry is not supported!");
    }

}

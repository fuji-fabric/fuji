package mod.fuji.module.initializer.works.config.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import mod.fuji.module.initializer.works.structure.WorkType;
import mod.fuji.module.initializer.works.structure.work.abst.Work;
import mod.fuji.module.initializer.works.structure.work.impl.NonProductionWork;
import mod.fuji.module.initializer.works.structure.work.impl.ProductionWork;
import java.lang.reflect.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorkTypeAdapter implements JsonDeserializer<Work> {

    @Override
    public @Nullable Work deserialize(@NotNull JsonElement json, Type typeOfT, @NotNull JsonDeserializationContext context) throws JsonParseException {
        String type = json.getAsJsonObject().get("type").getAsString();

        if (type.equals(WorkType.NonProductionWork.name()))
            return context.deserialize(json, NonProductionWork.class);

        if (type.equals(WorkType.ProductionWork.name()))
            return context.deserialize(json, ProductionWork.class);

        throw new JsonParseException("Unknown work type: " + type);
    }
}

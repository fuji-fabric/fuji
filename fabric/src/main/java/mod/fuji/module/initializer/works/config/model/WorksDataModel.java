package mod.fuji.module.initializer.works.config.model;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.works.structure.work.abst.Work;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CopyOnWriteArrayList;

public class WorksDataModel {

    @Document(id = 1751825466814L, value = "The created `work` list.")
    public @NotNull CopyOnWriteArrayList<Work> works = new CopyOnWriteArrayList<>();

}

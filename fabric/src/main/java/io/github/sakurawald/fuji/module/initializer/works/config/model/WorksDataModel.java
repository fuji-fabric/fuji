package io.github.sakurawald.fuji.module.initializer.works.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.works.structure.work.abst.Work;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WorksDataModel {

    @Document("The created `work` list.")
    public @NotNull List<Work> works = new CopyOnWriteArrayList<>();

}

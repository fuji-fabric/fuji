package io.github.sakurawald.fuji.module.initializer.afk.accessor;

public interface AfkStateAccessor {

    boolean fuji$isAfk();

    void fuji$changeAfk(boolean afk);

    void fuji$incrInputCounter();

    long fuji$getInputCounter();

}

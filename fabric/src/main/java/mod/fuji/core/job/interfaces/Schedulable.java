package mod.fuji.core.job.interfaces;

import mod.fuji.core.document.annotation.ForDeveloper;

/**
 *     This interface provides a method `onSchedule`.
    The typical use-case is: You have a job, to dispatch the onSchedule() message to each object that implements this interface.

 **/
public interface Schedulable {
    void onSchedule();
}

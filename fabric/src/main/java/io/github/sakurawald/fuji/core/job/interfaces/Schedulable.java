package io.github.sakurawald.fuji.core.job.interfaces;

import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;

@ForDeveloper("""
    This interface provides a method `onSchedule`.
    The typical use-case is: You have a job, to dispatch the onSchedule() message to each object that implements this interface.
    """)
public interface Schedulable {
    void onSchedule();
}

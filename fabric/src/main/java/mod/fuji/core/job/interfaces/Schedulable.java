package mod.fuji.core.job.interfaces;


/**
 *     This interface provides a method <code>onSchedule</code>.
    The typical use-case is: You have a job, to dispatch the onSchedule() message to each object that implements this interface.

 **/
public interface Schedulable {
    void onSchedule();
}

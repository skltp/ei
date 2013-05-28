package se.skltp.ei.intsvc.log;


/**
 * CustomEventLogger introduced to be able to control what info and error queues
 * to be able to use for logging. When this feature is introduced in soi-toolkit
 * DefaultEventLogger, CustomEventLogger class is no longer needed.
 */
public class CustomEventLogger extends EiOverrideDefaultEventLogger {

    private String infoEventQueue;

    private String errorEventQueue;

    public void setErrorEventQueue(String errorEventQueue) {
        this.errorEventQueue = errorEventQueue;
    }

    public void setInfoEventQueue(String infoEventQueue) {
        this.infoEventQueue = infoEventQueue;
    } 

    @Override
    protected void dispatchInfoEvent(String msg) {
        dispatchEvent(infoEventQueue, msg);
    }

    @Override
    protected void dispatchErrorEvent(String msg) {
        dispatchEvent(errorEventQueue, msg);
    }
}

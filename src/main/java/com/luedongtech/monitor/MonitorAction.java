package com.luedongtech.monitor;

/**
 * Created by zombie on 14/12/4.
 */
public enum MonitorAction {
    CLICK("click"),
    JUMP("jump"),
    NETWORK("network"),
    CALLBACK("callback"),
    LOCATION("location"),
    LIFECYCLE("lifecycle"),
    PUSH("push"),
    REFIT("refit");

    private String action;

    private MonitorAction(String arg) {
        action = arg;
    }

    public String toString() {
        return action;
    }
}

package com.luedongtech.monitor;

import android.os.Bundle;
import android.os.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zombie on 14/12/4.
 */
public class MonitorWork {
    private static final String SEPARATOR = "|";
    private static final String SEPARATOR_REPLACE = " ";

    /**
     * 事件
     * @param action
     *      事件类型
     * @param eventSource
     *      事件源
     * @param container
     *      容器
     * @param intention
     *      意图
     */
    public static void Action(MonitorAction action, String eventSource, String container, String intention) {
        Action(action, eventSource, container, intention, new HashMap());
    }

    /**
     * 事件
     * @param action
     *      事件类型
     * @param eventSource
     *      事件源
     * @param container
     *      容器
     * @param intention
     *      意图
     * @param status
     *      现场状态
     */
    public static void Action(MonitorAction action, String eventSource, String container, String intention, Map status) {
        Action(action, getTime(), eventSource, container, intention, status);
    }

    /**
     * 事件
     * @param action
     *      事件类型
     * @param time
     *      时间
     * @param eventSource
     *      事件源
     * @param container
     *      容器
     * @param intention
     *      意图
     * @param status
     *      现场状态
     */
    public static void Action(MonitorAction action, String time, String eventSource, String container, String intention, Map status) {
        Action(action, time, eventSource, container, "", intention, status);
    }

    /**
     * 事件
     * @param action
     *      事件类型
     * @param time
     *      时间
     * @param eventSource
     *      事件源
     * @param container
     *      容器
     * @param target
     *      目标容器
     * @param intention
     *      意图
     * @param status
     *      现场状态
     */
    public static void Action(MonitorAction action, String time, String eventSource, String container, String target, String intention, Map status) {
        if (MonitorService.Instance == null) {
            return;
        }
        if (time != null && time.contains(SEPARATOR)) {
            time = time.replaceAll(SEPARATOR, SEPARATOR_REPLACE);
        }
        if (eventSource != null && eventSource.contains(SEPARATOR)) {
            eventSource = eventSource.replaceAll(SEPARATOR, SEPARATOR_REPLACE);
        }
        if (container != null && container.contains(SEPARATOR)) {
            container = container.replaceAll(SEPARATOR, SEPARATOR_REPLACE);
        }
        if (target != null && target.contains(SEPARATOR)) {
            target = target.replaceAll(SEPARATOR, SEPARATOR_REPLACE);
        }
        if (intention != null && intention.contains(SEPARATOR)) {
            intention = intention.replaceAll(SEPARATOR, SEPARATOR_REPLACE);
        }
        if (status == null) {
            status = new HashMap();
        }

        StringBuilder stringBuilder = new StringBuilder(action.toString());
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(time);
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(eventSource);
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(container);
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(target);
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(intention);
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(status.toString());

        Bundle bundle = new Bundle();
        bundle.putString("content", stringBuilder.toString());
        Message message = new Message();
        message.what = MonitorService.WHAT_LOG;
        message.setData(bundle);
        MonitorService.Instance.getHandler().sendMessage(message);
    }

    /**
     * 当前时间
     * @return
     */
    public static String getTime() {
        return MonitorUtils.getCurrentTimeStr();
    }
}

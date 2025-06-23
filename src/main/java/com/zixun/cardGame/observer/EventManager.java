package com.zixun.cardGame.observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager {

    // 自定义范式EventListener
    private static final Map<Class<?>, List<EventListener<?>>> listeners = new HashMap<>();

    // 注册监听器
    public static <T> void addListener(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    // 移除监听器
    public static <T> void removeListener(Class<T> eventType, EventListener<T> listener) {
        List<EventListener<?>> list = listeners.get(eventType);
        if (list != null) {
            list.remove(listener);
        }
    }

    // 触发事件
    @SuppressWarnings("unchecked")
    public static <T> void fireEvent(T event) {
        List<EventListener<?>> list = listeners.get(event.getClass());
        if (list != null) {
            for (EventListener<?> listener : list) {
                ((EventListener<T>) listener).onEvent(event);
            }
        }
    }
}

package com.zixun.cardGame.observer;

public interface EventListener<T> {
    void onEvent(T event);
}

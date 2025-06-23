package com.zixun.cardGame.observer;

public interface Observer<T> {
    void onChange(T source);
}

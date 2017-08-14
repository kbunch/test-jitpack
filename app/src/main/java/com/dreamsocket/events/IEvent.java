package com.dreamsocket.events;


public interface IEvent {
    boolean isCanceled();
    void cancel();
}

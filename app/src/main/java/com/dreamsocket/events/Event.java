package com.dreamsocket.events;



public class Event implements IEvent{

    private boolean m_canceled;

    public boolean isCanceled(){
        return this.m_canceled;
    }

    public void cancel(){
        this.m_canceled = true;
    }
}

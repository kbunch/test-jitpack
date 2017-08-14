package com.dreamsocket.rx;


import io.reactivex.Observable;

public interface IRxBus {

    // SUBSCRIBE - only allows one subscription for a specific event/context combo
    @SuppressWarnings("unchecked")
    <T> Observable<T> on(Class<T> p_class, Object p_context);

    @SuppressWarnings("unchecked")
    <T> Observable<T> on(Class<T> p_class, Object p_context, int p_priority);

    // REMOVE ALL
    void off();

    // REMOVE ALL EVENTS FOR A TYPE
    <T> void off(Class<T> p_class);

    // REMOVE SPECIFIC EVENT FOR A CONTEXT
    <T> void off(Class<T> p_class, Object p_context);

    // REMOVE ALL EVENTS FOR A CONTEXT
    void off(Object p_context);

    <T> void post(T p_value);

}
package com.dreamsocket.rx;

import com.jakewharton.rxrelay2.PublishRelay;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;


public class RxBus implements IRxBus{
    private final ConcurrentHashMap<Class<?>, ConcurrentSkipListSet<SubscriptionEntry>> m_dispatchers;

    public RxBus(){
        this.m_dispatchers = new ConcurrentHashMap<>();
    }


    public boolean hasObservers(){
        return this.m_dispatchers.size() > 0;
    }


    public <T> boolean hasObservers(Class<T> p_class){
        return this.m_dispatchers.containsKey(p_class);
    }


    // SUBSCRIBE - only allows one subscription for a specific event/context combo
    @SuppressWarnings("unchecked")
    public <T> Observable<T> on(Class<T> p_class, Object p_context){
        return this.on(p_class, p_context, 0);
    }

    // SUBSCRIBE - only allows one subscription for a specific event/context combo
    // priority - The higher the number, the higher the priority. The default priority is 0
    @SuppressWarnings("unchecked")
    public <T> Observable<T> on(Class<T> p_class, Object p_context, int p_priority){
        return this.getSubscriptionEntry(p_class, p_context, p_priority).subject;
    }


    @SuppressWarnings("unchecked")
    public <T> void post(T p_value){
        Set<SubscriptionEntry> entries = this.m_dispatchers.get(p_value.getClass());

        if (entries != null && entries.size() > 0) {
            Iterator<SubscriptionEntry> itr = entries.iterator();

            while(itr.hasNext() && entries.size() > 0){
                itr.next().subject.accept(p_value);
            }
        }
    }


    // REMOVE ALL
    public void off(){
        Iterator<Class<?>> itr = this.m_dispatchers.keySet().iterator();

        while (itr.hasNext()) {
            this.off(itr.next());
        }

        this.m_dispatchers.clear();
    }


    // REMOVE ALL EVENTS FOR A TYPE
    public <T> void off(Class<T> p_class){
        Set<SubscriptionEntry> entries = this.m_dispatchers.remove(p_class);

        if (entries != null) {
            Iterator<SubscriptionEntry> iterator = entries.iterator();

            while (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
    }


    // REMOVE SPECIFIC EVENT FOR A CONTEXT
    public <T> void off(Class<T> p_class, Object p_context){
        Set<SubscriptionEntry> entries = this.m_dispatchers.get(p_class);

        if (entries != null) {
            Iterator<SubscriptionEntry> iterator = entries.iterator();

            while (iterator.hasNext()) {
                SubscriptionEntry entry = iterator.next();
                Object ctx = entry.context.get();

                if (ctx == p_context || ctx == null) {
                    // event has a subscription from context remove it
                    iterator.remove();
                }
            }

            // specific event no longer has subscriptions, clear it
            if (entries.size() == 0) {
                this.m_dispatchers.remove(p_class);
            }
        }
    }


    // REMOVE ALL EVENTS FOR A CONTEXT
    public void off(Object p_context){
        Iterator<Class<?>> itr = this.m_dispatchers.keySet().iterator();

        while (itr.hasNext()) {
            this.off(itr.next(), p_context);
        }
    }


    @SuppressWarnings("unchecked")
    private <T> SubscriptionEntry<T> getSubscriptionEntry(Class<T> p_class, Object p_context, int p_priority){
        SubscriptionEntry<T> entry = new SubscriptionEntry(p_context, p_priority);

        this.off(p_class, p_context);

        if(!this.hasObservers(p_class)){
            this.m_dispatchers.put(p_class, new ConcurrentSkipListSet<>());
        }

        this.m_dispatchers.get(p_class).add(entry);

        return entry;
    }



    private final static class SubscriptionEntry<T> implements Comparable<SubscriptionEntry<T>>{

        private final static AtomicInteger k_index = new AtomicInteger();

        public final WeakReference<Object> context;
        public final int index;
        public final int priority;
        public final PublishRelay<T> subject;

        public SubscriptionEntry(Object p_context, int p_priority){
            this.context = new WeakReference<>(p_context);
            this.index = k_index.incrementAndGet();
            this.priority = p_priority;
            this.subject = PublishRelay.create();
        }

        @Override
        public int compareTo(SubscriptionEntry<T> p_item){
            // add comparable to make sure listeners are ordered
            int priority = p_item.priority - this.priority;

            return priority == 0 ? this.index - p_item.index : priority;
        }
    }
}

package com.dreamsocket.rx;


import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;


import io.reactivex.disposables.Disposable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;


public class RxBusTest {

    private RxBus m_bus;
    private ArrayList<Event> m_events;
    private ArrayList<Listener> m_listeners;


    @Before public void setUp() throws Exception {
        this.m_bus = new RxBus();
        this.m_events = new ArrayList<>();
        this.m_listeners = new ArrayList<>();
    }


    @Test
    public void hasObservablesValid(){
        this.m_bus.on(Event1.class, this);

        assertEquals("Subscribers not added", true, this.m_bus.hasObservers());
    }


    @Test
    public void hasObservablesForClassValid(){
        this.m_bus.on(Event1.class, this);

        assertEquals("Subscribers not added for specific class", true, this.m_bus.hasObservers(Event1.class));
        assertEquals("Showing subscribers or incorrect class", false, this.m_bus.hasObservers(Event2.class));
    }


    @Test
    public void checkEventType(){
        Listener listener = new Listener(this.m_events, this.m_listeners, 0);

        this.m_bus.on(Event1.class, this).subscribe(listener::onEvent1Fired);
        this.m_bus.post(new Event1(1));

        assertEquals("Expected Event1", this.m_events.get(0) instanceof Event1, true);
    }


    @Test
    public void checkEventCount(){
        Listener listener = new Listener(this.m_events, this.m_listeners, 0);

        this.m_bus.on(Event1.class, this).subscribe(listener::onEvent1Fired);
        this.m_bus.post(new Event1(1));
        this.m_bus.post(new Event1(1));

        assertEquals("Expected 2 events to be added", this.m_events.size(), 2);
    }


    @Test
    public void checkEventOrder(){
        Listener listener = new Listener(this.m_events, this.m_listeners, 0);

        this.m_bus.on(Event1.class, this).subscribe(listener::onEvent1Fired);
        this.m_bus.post(new Event1(1));
        this.m_bus.post(new Event1(2));

        assertEquals("Expected 1st event index 1", this.m_events.get(0).index, 1);
        assertEquals("Expected 2nd event index 2", this.m_events.get(1).index, 2);
    }


    @Test
    public void checkEventPriority(){
        for(int i = 0; i < 20; i++){
            Listener listener = new Listener(this.m_events, this.m_listeners, i);

            this.m_bus.on(Event1.class, listener, i == 10 ? 1 : 0).subscribe(test -> listener.onEvent1Fired(test));
        }

        this.m_bus.post(new Event1(1));

        assertEquals("Expected 1st listener as index 10", this.m_listeners.get(0).index, 10);
        assertEquals("Expected 2nd listener as index 0", this.m_listeners.get(1).index, 0);
    }


    @Test
    public void checkMultipleEventsAreSubscribed(){
        Listener listener = new Listener(this.m_events, this.m_listeners, 0);

        this.m_bus.on(Event1.class, this).subscribe(listener::onEvent1Fired);
        this.m_bus.on(Event2.class, this).subscribe(listener::onEvent2Fired);
        this.m_bus.post(new Event1(1));
        this.m_bus.post(new Event2(1));

        assertEquals("Expected 1st event as Event1", this.m_events.get(0) instanceof Event1, true);
        assertEquals("Expected 2nd event as Event2", this.m_events.get(1) instanceof Event2, true);
    }


    @Test
    public void checkAllListenersCleared(){
        for(int i = 0; i < 20; i++) {
            Listener listener = new Listener(this.m_events, this.m_listeners, i);

            this.m_bus.on(Event1.class, this).subscribe(listener::onEvent1Fired);
            this.m_bus.on(Event2.class, this).subscribe(listener::onEvent2Fired);
        }

        this.m_bus.off();
        this.m_bus.post(new Event1(1));
        this.m_bus.post(new Event2(1));

        assertEquals("Expected no events to be fired", this.m_events.size(), 0);
    }


    @Test
    public void checkContextListenersCleared(){
        Object context1 = new Object();
        Object context2 = new Object();

        for(int i = 0; i < 20; i++) {
            Listener listener = new Listener(this.m_events, this.m_listeners, i);

            this.m_bus.on(Event1.class, context1).subscribe(listener::onEvent1Fired);
            this.m_bus.on(Event2.class, context2).subscribe(listener::onEvent2Fired);
        }

        this.m_bus.off(context1);
        this.m_bus.post(new Event1(1));
        this.m_bus.post(new Event2(1));

        for(Event event : this.m_events){
            if(event instanceof  Event1){
                fail("Expected no Event1 events to be fired for context1");
                break;
            }
        }

        assertNotEquals("Expected Event2 events to be fired for context2", this.m_events.size(), 0);
    }


    @Test
    public void checkContextClassListenersCleared(){
        Object context1 = new Object();
        Object context2 = new Object();

        for(int i = 0; i < 20; i++) {
            int index = i % 2 == 0 ? 0 : 1;
            Object ctx = i % 2 == 0 ? context1 : context2;
            Listener listener = new Listener(this.m_events, this.m_listeners, index);

            this.m_bus.on(Event1.class, ctx).subscribe(listener::onEvent1Fired);
        }

        this.m_bus.off(context1);
        this.m_bus.post(new Event1(1));

        for(Event event : this.m_events){
            if(event.index == 0){
                fail("Expected no Event1 events to be fired for context1");
                break;
            }
        }
    }


    @Test
    public void checkClassListenersCleared(){
        for(int i = 0; i < 20; i++) {
            Listener listener = new Listener(this.m_events, this.m_listeners, i);

            this.m_bus.on(Event1.class, this).subscribe(listener::onEvent1Fired);
            this.m_bus.on(Event2.class, this).subscribe(listener::onEvent2Fired);
        }

        this.m_bus.off(Event1.class);
        this.m_bus.post(new Event1(1));
        this.m_bus.post(new Event2(1));

        for(Event event : this.m_events){
            if(event instanceof  Event1){
                assertEquals("Expected no Event1 events to be fired", this.m_events.size(), 0);
                break;
            }
        }
    }


    @Test
    public void checkReentrantListenersCleared(){
        for(int i = 0; i < 20; i++) {
            ReentrantListener listener = new ReentrantListener(this.m_events, () -> this.m_bus.off());

            this.m_bus.on(Event1.class, this).subscribe(listener::onEvent1Fired);
        }

        this.m_bus.post(new Event1(1));


        assertEquals("Expected 1 event to be fired", this.m_events.size(), 1);
    }



    @Test
    public void checkDisposedListener(){
        Listener listener = new Listener(this.m_events, this.m_listeners, 0);

        Disposable disposable = this.m_bus.on(Event1.class, this).subscribe(listener::onEvent1Fired);

        disposable.dispose();

        this.m_bus.post(new Event1(1));


        assertEquals("Expected no events to be fired", this.m_events.size(), 0);
    }



    // Simple Action lambda
    protected interface Action{
        void perform();
    }



    // Helper Listeners
    protected class Listener{

        protected final ArrayList<Event> m_events;
        protected final ArrayList<Listener> m_listeners;


        public Listener(ArrayList<Event> p_events, ArrayList<Listener> p_listeners, int p_index){
            this.m_events = p_events;
            this.m_listeners = p_listeners;
            this.index = p_index;
        }

        public final int index;

        protected void onEvent1Fired(Event1 p_event){
            this.m_events.add(p_event);
            this.m_listeners.add(this);
        }

        protected void onEvent2Fired(Event2 p_event){
            this.m_events.add(p_event);
            this.m_listeners.add(this);
        }
    }



    protected class ReentrantListener{

        protected final Action m_action;
        protected final ArrayList<Event> m_events;


        public ReentrantListener(ArrayList<Event> p_events, Action p_action){
            this.m_events = p_events;
            this.m_action = p_action; 
        }

        protected void onEvent1Fired(Event1 p_event){
            this.m_events.add(p_event);
            this.m_action.perform();
        }
    }


    // Helper Events

    public class Event{
        public Event(int p_index){
            this.index = p_index;
        }

        public final int index;
    }


    public class Event1 extends Event{
        public Event1(int p_index){super(p_index);}
    }


    public class Event2 extends Event{
        public Event2(int p_index){super(p_index);}
    }
}

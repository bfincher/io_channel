package com.fincher.iochannel;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Listeners<ListenerType, DataType> {

    private final List<ListenerEntry> listeners = new LinkedList<>();

    public void addListener(ListenerType listener) {
        listeners.add(new ListenerEntry(listener));
    }


    public void addListener(ListenerType listener, Predicate<DataType> predicate) {
        listeners.add(new ListenerEntry(listener, predicate));
    }


    public boolean removeListener(ListenerType listener) {
        return listeners.removeIf(entry -> entry.listener.equals(listener));
    }


    public Stream<ListenerType> getListenersThatMatch(DataType data) {
        return listeners.stream().filter(l -> l.predicate.test(data)).map(l -> l.listener);
    }


    public Stream<ListenerType> getListeners() {
        return listeners.stream().map(l -> l.listener);
    }

    private class ListenerEntry {
        private final ListenerType listener;
        private final Predicate<DataType> predicate;

        ListenerEntry(ListenerType listener, Predicate<DataType> predicate) {
            this.listener = listener;
            this.predicate = predicate;
        }


        ListenerEntry(ListenerType listener) {
            this(listener, t -> true);
        }
    }

}

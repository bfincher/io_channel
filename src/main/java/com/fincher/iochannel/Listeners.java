package com.fincher.iochannel;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/** An abstraction of registered listeners
 * 
 * @author bfincher
 *
 * @param <L> The listener type
 * @param <D> The type of data the listener is registering for
 */
public class Listeners<L, D> {

    private final List<ListenerEntry> listenerList = new LinkedList<>();

    /** Add a listener
     * 
     * @param listener The lisener
     */
    public void addListener(L listener) {
        listenerList.add(new ListenerEntry(listener));
    }


    /** Add a listener
     * 
     * @param listener The listener
     * @param predicate A predicate such that the listener will only be notified if the predicate matches
     */
    public void addListener(L listener, Predicate<D> predicate) {
        listenerList.add(new ListenerEntry(listener, predicate));
    }


    /** Remove a listener
     * 
     * @param listener The listener to be removed
     * @return true if a listener was removed
     */
    public boolean removeListener(L listener) {
        return listenerList.removeIf(entry -> entry.listener.equals(listener));
    }


    /** Get all listeners that match the predicate.  If there is not a predicate, all listeners 
     * will be returned
     * @param data The data to be matched by the predicate
     * @return listeners that match the predicate.  If there is not a predicate, all listeners
     */
    public Stream<L> getListenersThatMatch(D data) {
        return listenerList.stream().filter(l -> l.predicate.test(data)).map(l -> l.listener);
    }


    /** Get all listeners
     * 
     * @return all listeners
     */
    public Stream<L> getListeners() {
        return listenerList.stream().map(l -> l.listener);
    }

    private class ListenerEntry {
        private final L listener;
        private final Predicate<D> predicate;

        ListenerEntry(L listener, Predicate<D> predicate) {
            this.listener = listener;
            this.predicate = predicate;
        }


        ListenerEntry(L listener) {
            this(listener, t -> true);
        }
    }

}

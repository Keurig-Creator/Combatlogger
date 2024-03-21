package com.keurig.combatlogger.utils;

import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

public class TempHashSet<E> extends HashSet<E> {
    private final long ttl; // Time-to-live for elements in milliseconds

    public TempHashSet(long ttl) {
        this.ttl = ttl;
    }

    // Override add method to schedule removal after ttl
    @Override
    public boolean add(E e) {
        boolean added = super.add(e);
        if (added) {
            scheduleRemoval(e);
        }
        return added;
    }

    // Schedule removal of element after ttl
    private void scheduleRemoval(final E element) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                remove(element);
            }
        }, ttl);
    }
}
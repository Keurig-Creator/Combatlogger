package com.keurig.combatlogger.utils;

import java.util.HashSet;
import java.util.Iterator;
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

    // For testing purposes: print the set
    public void printSet() {
        Iterator<E> iterator = iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }

    // Example usage
    public static void main(String[] args) {
        TempHashSet<String> tempSet = new TempHashSet<>(5000); // TTL of 5 seconds

        tempSet.add("Value1");
        tempSet.add("Value2");

        tempSet.printSet(); // Print initial set

        // Wait for TTL to expire
        try {
            Thread.sleep(6000); // Wait for 6 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        tempSet.printSet(); // Print set after TTL expiration
    }
}
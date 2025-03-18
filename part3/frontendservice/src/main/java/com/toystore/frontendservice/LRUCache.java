package com.toystore.frontendservice;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> extends LinkedHashMap<K, V> { //AI Generated Code
    private int capacity;

    public LRUCache(int capacity) {
        super(capacity, 0.75f, true); // Initial capacity, load factor, and access order
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity; // This will remove the least recently accessed item
    }

    public synchronized V getCache(K key) {
        return super.getOrDefault(key, null);
    }

    public synchronized void putCache(K key, V value) {
        super.put(key, value);
    }

    public synchronized void invalidateCache(K key) {
        super.remove(key);
    }
    
    public synchronized void printCache() {
        System.out.println("Current Cache State:");
        for (Map.Entry<K, V> entry : entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
    }
}

package io.github.kdesp73;

import java.util.HashMap;
import java.util.Map;

public class LocalStorage {
    private static LocalStorage instance;
    private final Map<String, String> storage;

    private LocalStorage() {
        storage = new HashMap<>();
    }

    public static synchronized LocalStorage getInstance() {
        if (instance == null) {
            instance = new LocalStorage();
        }
        return instance;
    }

    public void setItem(String key, String value) {
        storage.put(key, value);
    }

    public String getItem(String key) {
        return storage.get(key);
    }

    public void removeItem(String key) {
        storage.remove(key);
    }

    public void clear() {
        storage.clear();
    }
}
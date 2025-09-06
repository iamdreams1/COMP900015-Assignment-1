package org.dictionary;

// --- DictionaryManager.java ---
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DictionaryManager {
    private final Map<String, List<String>> dictionary;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final String filePath;
    private final Gson gson = new Gson();

    public DictionaryManager(String filePath) {
        this.filePath = filePath;
        this.dictionary = new ConcurrentHashMap<>();
        loadDictionaryFromFile();
    }

    private void loadDictionaryFromFile() {
        lock.writeLock().lock();
        try (FileReader reader = new FileReader(filePath)) {
            Type type = new TypeToken<Map<String, List<String>>>(){}.getType();
            Map<String, List<String>> loadedMap = gson.fromJson(reader, type);
            if (loadedMap != null) {
                dictionary.putAll(loadedMap);
            }
        } catch (IOException e) {
            System.out.println("Dictionary file not found or is empty. Starting with a new dictionary.");
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void saveDictionaryToFile() {
        // This method should be called by any method that modifies the dictionary.
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(dictionary, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> query(String word) {
        lock.readLock().lock();
        try {
            return dictionary.get(word);
        } finally {
            lock.readLock().unlock();
        }
    }

    public String add(String word, List<String> meanings) {
        lock.writeLock().lock();
        try {
            if (dictionary.containsKey(word)) {
                return "DUPLICATE";
            }
            dictionary.put(word, meanings);
            saveDictionaryToFile();
            return "SUCCESS";
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String remove(String word) {
        lock.writeLock().lock();
        try {
            if (!dictionary.containsKey(word)) {
                return "NOT_FOUND";
            }
            dictionary.remove(word);
            saveDictionaryToFile();
            return "SUCCESS";
        } finally {
            lock.writeLock().unlock();
        }
    }

    // TODO: Implement methods for 'update meaning' and 'add new meaning'
}

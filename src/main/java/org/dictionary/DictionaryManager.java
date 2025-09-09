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

    public String add(String word, List<String> meanings, long delay) {
        lock.writeLock().lock();
        try {
            if (dictionary.containsKey(word)) {
                return "DUPLICATE";
            }
            dictionary.put(word, meanings);
            saveDictionaryToFile();

            if (delay > 0) {
                System.out.println("Lock held for " + delay + "ms for simulated slow write...");
                Thread.sleep(delay);
            }
            return "SUCCESS";
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String remove(String word, long delay) {
        lock.writeLock().lock();
        try {
            if (!dictionary.containsKey(word)) {
                return "NOT_FOUND";
            }
            dictionary.remove(word);
            saveDictionaryToFile();

            if (delay > 0) {
                System.out.println("Lock held for " + delay + "ms for simulated slow write...");
                Thread.sleep(delay);
            }
            return "SUCCESS";
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String updateMeaning(String word, String meaningToBeUpdated, String newMeaning, long delay) {
        lock.writeLock().lock();
        try {
            // 2. Check if the word exists in the dictionary.
            List<String> meanings = dictionary.get(word);
            if (meanings == null) {
                return "WORD_NOT_FOUND";
            }

            // 3. Find the index of the specific meaning that needs to be updated.
            int index = meanings.indexOf(meaningToBeUpdated);
            if (index == -1) {
                // The specific meaning was not found in the list.
                return "MEANING_NOT_FOUND";
            }

            if (meanings.contains(newMeaning)) {
                return "MEANING_EXISTS";
            }

            // 4. Update the meaning at the found index with the new meaning.
            meanings.set(index, newMeaning);

            // 5. Save the changes to the JSON file to make them persistent.
            saveDictionaryToFile();

            if (delay > 0) {
                System.out.println("Lock held for " + delay + "ms for simulated slow write...");
                Thread.sleep(delay);
            }

            // 6. Return a success status.
            return "SUCCESS";

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 7. Always release the lock in a finally block.
            lock.writeLock().unlock();
        }
    }

    public String addNewMeaning(String word, String newMeaning, long delay) {
        lock.writeLock().lock();
        try {
            // 2. Check if the word exists in the dictionary.
            List<String> meanings = dictionary.get(word);
            if (meanings == null) {
                return "WORD_NOT_FOUND";
            }

            // 3. Check if the meaning already exists to prevent duplicates.
            if (meanings.contains(newMeaning)) {
                return "MEANING_EXISTS";
            }

            // 4. Add the new meaning to the existing list.
            meanings.add(newMeaning);

            // 5. Save the changes to the file.
            saveDictionaryToFile();
            if (delay > 0) {
                System.out.println("Lock held for " + delay + "ms for simulated slow write...");
                Thread.sleep(delay);
            }
            // 6. Return a success status.
            return "SUCCESS";

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 7. Always release the lock.
            lock.writeLock().unlock();
        }
    }
}

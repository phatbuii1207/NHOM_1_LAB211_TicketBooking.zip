package repository;

import model.BaseEntity;
import java.io.*;
import java.util.*;
import java.util.function.Predicate;

public abstract class CsvRepository<T extends BaseEntity> {
    protected String filePath;
    protected List<T> cache;
    protected boolean cacheLoaded = false;

    public CsvRepository(String filePath) {
        this.filePath = filePath;
    }

    protected abstract T parseFromCsvLine(String line);

    protected abstract String parseToCsvLine(T entity);

    protected abstract String getHeader();

    public List<T> findAll() {
        if (!cacheLoaded) {
            loadAllFromFile();
        }
        return new ArrayList<>(cache);
    }

    public T findById(String id) {
        return findAll().stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<T> findByCondition(Predicate<T> condition) {
        List<T> result = new ArrayList<>();
        for (T entity : findAll()) {
            if (condition.test(entity)) {
                result.add(entity);
            }
        }
        return result;
    }

    public boolean save(T entity) {
        List<T> entities = findAll();
        if (findById(entity.getId()) != null) {
            return false;
        }
        entities.add(entity);
        return saveAllToFile(entities);
    }

    public boolean update(T entity) {
        List<T> entities = findAll();
        for (int i = 0; i < entities.size(); i++) {
            if (entities.get(i).getId().equals(entity.getId())) {
                entities.set(i, entity);
                return saveAllToFile(entities);
            }
        }
        return false;
    }

    public boolean delete(String id) {
        List<T> entities = findAll();
        boolean removed = entities.removeIf(e -> e.getId().equals(id));
        if (removed) {
            return saveAllToFile(entities);
        }
        return false;
    }

    protected void loadAllFromFile() {
        cache = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            cacheLoaded = true;
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                T entity = parseFromCsvLine(line);
                if (entity != null) {
                    cache.add(entity);
                }
            }
            cacheLoaded = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected boolean saveAllToFile(List<T> entities) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(getHeader());
            writer.newLine();
            for (T entity : entities) {
                writer.write(parseToCsvLine(entity));
                writer.newLine();
            }
            cache = new ArrayList<>(entities);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void reload() {
        cacheLoaded = false;
        findAll();
    }
}

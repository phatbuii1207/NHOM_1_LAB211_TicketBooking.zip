package repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import model.Stadium;

public class StadiumRepository implements ICsvRepository<Stadium> {

    private final CsvRepository<Stadium> csv;

    public StadiumRepository() {
        this.csv = new CsvRepository<>("data/stadiums.csv", Stadium::new);
    }

    public StadiumRepository(String filePath) {
        this.csv = new CsvRepository<>(filePath, Stadium::new);
    }

    @Override
    public List<Stadium> findAll() {
        return csv.findAll();
    }

    @Override
    public Optional<Stadium> findById(String id) {
        return csv.findById(id);
    }

    @Override
    public List<Stadium> findByCondition(Predicate<Stadium> condition) {
        return csv.findByCondition(condition);
    }

    @Override
    public boolean save(Stadium entity) {
        return csv.save(entity);
    }

    @Override
    public boolean deleteById(String id) {
        return csv.deleteById(id);
    }

    @Override
    public int count() {
        return csv.count();
    }
}

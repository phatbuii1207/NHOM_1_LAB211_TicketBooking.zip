package repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import model.Section;

public class SectionRepository implements ICsvRepository<Section> {

    private final CsvRepository<Section> csv;

    public SectionRepository() {
        this.csv = new CsvRepository<>("data/sections.csv", Section::new);
    }

    public SectionRepository(String filePath) {
        this.csv = new CsvRepository<>(filePath, Section::new);
    }

    public List<Section> findByStadiumId(String stadiumId) {
        return findByCondition(s -> s.getStadiumId().equals(stadiumId));
    }

    @Override
    public List<Section> findAll() {
        return csv.findAll();
    }

    @Override
    public Optional<Section> findById(String id) {
        return csv.findById(id);
    }

    @Override
    public List<Section> findByCondition(Predicate<Section> condition) {
        return csv.findByCondition(condition);
    }

    @Override
    public boolean save(Section entity) {
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

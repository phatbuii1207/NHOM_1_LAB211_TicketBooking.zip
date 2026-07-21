package repository;

import model.Section;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * SectionRepository – Repository cho Khu vuc san.
 * Doc/ghi data/sections.csv.
 */
public class SectionRepository implements ICsvRepository<Section> {

    private final CsvRepository<Section> csv;

    public SectionRepository() {
        this.csv = new CsvRepository<>("data/sections.csv", Section::new);
    }

    public SectionRepository(String filePath) {
        this.csv = new CsvRepository<>(filePath, Section::new);
    }

    @Override public List<Section>     findAll()                             { return csv.findAll(); }
    @Override public Optional<Section> findById(String id)                  { return csv.findById(id); }
    @Override public List<Section>     findByCondition(Predicate<Section> c) { return csv.findByCondition(c); }
    @Override public boolean           save(Section s)                      { return csv.save(s); }
    @Override public boolean           deleteById(String id)                { return csv.deleteById(id); }
    @Override public int               count()                              { return csv.count(); }

    /** Lay danh sach khu vuc theo san van dong */
    public List<Section> findByStadiumId(String stadiumId) {
        return csv.findByCondition(s -> stadiumId.equals(s.getStadiumId()));
    }
}

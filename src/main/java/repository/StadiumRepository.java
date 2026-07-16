package repository;

import model.Stadium;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * StadiumRepository – Repository cho San van dong.
 * Doc/ghi data/stadiums.csv.
 */
public class StadiumRepository implements ICsvRepository<Stadium> {

    private final CsvRepository<Stadium> csv;

    public StadiumRepository() {
        this.csv = new CsvRepository<>("data/stadiums.csv", Stadium::new);
    }

    public StadiumRepository(String filePath) {
        this.csv = new CsvRepository<>(filePath, Stadium::new);
    }

    @Override public List<Stadium>     findAll()                           { return csv.findAll(); }
    @Override public Optional<Stadium> findById(String id)                { return csv.findById(id); }
    @Override public List<Stadium>     findByCondition(Predicate<Stadium> c) { return csv.findByCondition(c); }
    @Override public boolean           save(Stadium s)                    { return csv.save(s); }
    @Override public boolean           deleteById(String id)              { return csv.deleteById(id); }
    @Override public int               count()                            { return csv.count(); }
}

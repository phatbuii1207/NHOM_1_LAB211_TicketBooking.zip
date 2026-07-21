package repository;

import model.Match;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * MatchRepository – Repository cho Tran dau.
 * Doc/ghi data/matches.csv.
 */
public class MatchRepository implements ICsvRepository<Match> {

    private final CsvRepository<Match> csv;

    public MatchRepository() {
        this.csv = new CsvRepository<>("data/matches.csv", Match::new);
    }

    public MatchRepository(String filePath) {
        this.csv = new CsvRepository<>(filePath, Match::new);
    }

    @Override public List<Match>     findAll()                          { return csv.findAll(); }
    @Override public Optional<Match> findById(String id)               { return csv.findById(id); }
    @Override public List<Match>     findByCondition(Predicate<Match> c) { return csv.findByCondition(c); }
    @Override public boolean         save(Match m)                     { return csv.save(m); }
    @Override public boolean         deleteById(String id)             { return csv.deleteById(id); }
    @Override public int             count()                           { return csv.count(); }

    /** Lay danh sach tran theo san van dong */
    public List<Match> findByStadiumId(String stadiumId) {
        return csv.findByCondition(m -> stadiumId.equals(m.getStadiumId()));
    }
}

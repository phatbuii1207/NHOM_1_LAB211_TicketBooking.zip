package repository;

import model.Ticket;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * TicketRepository – Quản lý vé xem trận đấu.
 *
 * Cách dùng:
 *   TicketRepository repo = new TicketRepository();
 *   List<Ticket> myTickets = repo.findByFanId("FAN0001");
 */
public class TicketRepository implements ICsvRepository<Ticket> {

    private final CsvRepository<Ticket> csv;

    public TicketRepository() {
        this.csv = new CsvRepository<>("data/tickets.csv", Ticket::new);
    }

    // Constructor cho test (truyền path tùy ý)
    public TicketRepository(String filePath) {
        this.csv = new CsvRepository<>(filePath, Ticket::new);
    }

    // ================================================================
    // CRUD CHUNG – delegate lên CsvRepository
    // ================================================================

    @Override public List<Ticket>     findAll()                          { return csv.findAll(); }
    @Override public Optional<Ticket> findById(String id)               { return csv.findById(id); }
    @Override public List<Ticket>     findByCondition(Predicate<Ticket> c) { return csv.findByCondition(c); }
    @Override public boolean          save(Ticket t)                    { return csv.save(t); }
    @Override public boolean          deleteById(String id)             { return csv.deleteById(id); }
    @Override public int              count()                           { return csv.count(); }

    // ================================================================
    // METHOD ĐẶC THÙ CHO TICKET
    // ================================================================

    /** Tất cả vé của 1 fan */
    public List<Ticket> findByFanId(String fanId) {
        return csv.findByCondition(t -> t.getFanId().equals(fanId));
    }

    /** Tất cả vé của 1 trận đấu */
    public List<Ticket> findByMatchId(String matchId) {
        return csv.findByCondition(t -> t.getMatchId().equals(matchId));
    }

    /** Tất cả vé của 1 ghế cụ thể */
    public List<Ticket> findBySeatId(String seatId) {
        return csv.findByCondition(t -> t.getSeatId().equals(seatId));
    }
}

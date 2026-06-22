package repository;

import model.BookingTransaction;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * TransactionRepository – Quản lý giao dịch thanh toán.
 */
public class TransactionRepository implements ICsvRepository<BookingTransaction> {

    private final CsvRepository<BookingTransaction> csv;

    public TransactionRepository() {
        this.csv = new CsvRepository<>("data/transactions.csv", BookingTransaction::new);
    }

    public TransactionRepository(String filePath) {
        this.csv = new CsvRepository<>(filePath, BookingTransaction::new);
    }

    // ================================================================
    // CRUD CHUNG
    // ================================================================

    @Override public List<BookingTransaction>     findAll()                                    { return csv.findAll(); }
    @Override public Optional<BookingTransaction> findById(String id)                         { return csv.findById(id); }
    @Override public List<BookingTransaction>     findByCondition(Predicate<BookingTransaction> c) { return csv.findByCondition(c); }
    @Override public boolean                      save(BookingTransaction t)                  { return csv.save(t); }
    @Override public boolean                      deleteById(String id)                      { return csv.deleteById(id); }
    @Override public int                          count()                                    { return csv.count(); }

    // ================================================================
    // METHOD ĐẶC THÙ
    // ================================================================

    /** Tất cả giao dịch của 1 fan */
    public List<BookingTransaction> findByFanId(String fanId) {
        return csv.findByCondition(t -> t.getFanId().equals(fanId));
    }

    /** Lọc theo trạng thái: PENDING / COMPLETED / CANCELLED / FAILED */
    public List<BookingTransaction> findByStatus(String status) {
        return csv.findByCondition(t -> t.getStatus().equalsIgnoreCase(status));
    }

    /** Tổng tiền fan đã thanh toán (status = COMPLETED) */
    public double totalAmountByFan(String fanId) {
        return findByCondition(t ->
            t.getFanId().equals(fanId) && "COMPLETED".equalsIgnoreCase(t.getStatus()))
            .stream().mapToDouble(BookingTransaction::getAmount).sum();
    }
}

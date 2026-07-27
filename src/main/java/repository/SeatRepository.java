package repository;

import model.Seat;
import model.SeatStatus;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * SeatRepository – Repository chuyên biệt cho Seat (Ghế ngồi).
 *
 * CÁCH DÙNG:
 *   SeatRepository repo  = new SeatRepository();
 *   List<Seat> available = repo.findAvailableSeats();
 *   List<Seat> section   = repo.findBySectionId("ST001_S01");
 *   boolean ok           = repo.lockSeat("SEAT000001");
 */
public class SeatRepository implements ICsvRepository<Seat> {

    private final CsvRepository<Seat> csv;

    public SeatRepository() {
        this.csv = new CsvRepository<>("data/seats.csv", Seat::new);
    }

    // Constructor cho test (truyền path tùy ý)
    public SeatRepository(String filePath) {
        this.csv = new CsvRepository<>(filePath, Seat::new);
    }

    // ================================================================
    // CRUD CHUNG – delegate lên CsvRepository
    // ================================================================

    @Override
    public List<Seat> findAll() {
        return csv.findAll();
    }

    @Override
    public Optional<Seat> findById(String id) {
        return csv.findById(id);
    }

    @Override
    public List<Seat> findByCondition(Predicate<Seat> condition) {
        return csv.findByCondition(condition);
    }

    @Override
    public boolean save(Seat seat) {
        return csv.save(seat);
    }

    public boolean saveAll(List<Seat> seats) {
        return csv.saveAll(seats);
    }

    @Override
    public boolean deleteById(String id) {
        return csv.deleteById(id);
    }

    @Override
    public int count() {
        return csv.count();
    }

    // ================================================================
    // METHOD ĐẶC THÙ CHO SEAT
    // ================================================================

    /**
     * Lấy tất cả ghế trong 1 khu vực (section).
     *
     * Ví dụ: findBySectionId("ST001_S01") → tất cả ghế của khu S01, sân ST001
     *
     * @param sectionId Mã khu vực (VD: "ST001_S01")
     */
    public List<Seat> findBySectionId(String sectionId) {
        return csv.findByCondition(s -> s.getSectionId().equals(sectionId));
    }

    /**
     * Lấy tất cả ghế đang còn trống (AVAILABLE).
     */
    public List<Seat> findAvailableSeats() {
        return csv.findByCondition(s -> s.getStatus() == SeatStatus.AVAILABLE);
    }

    /**
     * Lấy tất cả ghế đang bị khoá (LOCKED).
     */
    public List<Seat> findLockedSeats() {
        return csv.findByCondition(s -> s.getStatus() == SeatStatus.LOCKED);
    }

    /**
     * Lấy tất cả ghế đã đặt (BOOKED).
     */
    public List<Seat> findBookedSeats() {
        return csv.findByCondition(s -> s.getStatus() == SeatStatus.BOOKED);
    }

    /**
     * Lấy ghế trống trong 1 khu vực cụ thể.
     *
     * Tiện cho use-case: "Tìm ghế trống trong khu VIP"
     */
    public List<Seat> findAvailableInSection(String sectionId) {
        return csv.findByCondition(s ->
                s.getSectionId().equals(sectionId) &&
                s.getStatus() == SeatStatus.AVAILABLE);
    }

    /**
     * Khoá ghế theo id (AVAILABLE → LOCKED) và lưu lại file.
     *
     * @param seatId ID ghế cần khoá
     * @return true nếu khoá thành công, false nếu không tìm thấy hoặc không thể khoá
     */
    public boolean lockSeat(String seatId) {
        Optional<Seat> found = findById(seatId);
        if (found.isEmpty()) return false;

        Seat seat = found.get();
        if (!seat.lock()) return false;  // lock() trả false nếu ghế không phải AVAILABLE

        return save(seat);  // Lưu trạng thái mới vào file
    }

    /**
     * Mở khoá ghế (LOCKED → AVAILABLE) và lưu lại file.
     *
     * @param seatId ID ghế cần mở khoá
     * @return true nếu thành công
     */
    public boolean unlockSeat(String seatId) {
        Optional<Seat> found = findById(seatId);
        if (found.isEmpty()) return false;

        Seat seat = found.get();
        if (!seat.unlock()) return false;  // unlock() trả false nếu ghế không phải LOCKED

        return save(seat);
    }

    /**
     * Đặt ghế (AVAILABLE hoặc LOCKED → BOOKED) và lưu lại file.
     *
     * @param seatId ID ghế cần đặt
     * @return true nếu thành công
     */
    public boolean bookSeat(String seatId) {
        Optional<Seat> found = findById(seatId);
        if (found.isEmpty()) return false;

        Seat seat = found.get();
        if (!seat.book()) return false;  // book() trả false nếu ghế đã BOOKED

        return save(seat);
    }

    /**
     * Lưu ghế với cơ chế Optimistic Locking.
     * Kiểm tra xem version hiện tại trong file có bằng expectedVersion không.
     * Nếu bằng -> lưu (cập nhật trạng thái và version đã tăng) -> trả về true.
     * Nếu không bằng -> version đã thay đổi bởi thread khác -> trả về false (conflict).
     */
    public synchronized boolean saveOptimistic(Seat seat, int expectedVersion) {
        List<Seat> all = csv.findAll();
        for (int i = 0; i < all.size(); i++) {
            Seat current = all.get(i);
            if (current.getId().equals(seat.getId())) {
                if (current.getVersion() != expectedVersion) {
                    return false;
                }
                all.set(i, seat);
                return csv.saveAll(all);
            }
        }
        return false;
    }

    /**
     * Đếm ghế theo từng trạng thái.
     */
    public int countByStatus(SeatStatus status) {
        return (int) findAll().stream()
                .filter(s -> s.getStatus() == status)
                .count();
    }
}

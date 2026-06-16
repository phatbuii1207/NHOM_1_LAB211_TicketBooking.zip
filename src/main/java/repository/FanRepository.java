package repository;

import model.Fan;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * FanRepository – Repository chuyên biệt cho Fan.
 *
 * CÁCH HOẠT ĐỘNG:
 *   FanRepository "bọc ngoài" CsvRepository<Fan>.
 *   Mọi method CRUD chung (findAll, save...) được uỷ thác (delegate) cho CsvRepository.
 *   FanRepository chỉ thêm những method đặc thù cho Fan (findByEmail).
 *
 * CÁCH DÙNG:
 *   FanRepository repo = new FanRepository();
 *   Optional<Fan> fan  = repo.findByEmail("a@mail.com");
 *   List<Fan> gmails   = repo.findByCondition(f -> f.getEmail().endsWith("@gmail.com"));
 */
public class FanRepository implements ICsvRepository<Fan> {

    // CsvRepository xử lý toàn bộ đọc/ghi file CSV
    private final CsvRepository<Fan> csv;

    public FanRepository() {
        this.csv = new CsvRepository<>("data/fans.csv", Fan::new);
    }

    // Constructor cho test (truyền path tùy ý)
    public FanRepository(String filePath) {
        this.csv = new CsvRepository<>(filePath, Fan::new);
    }

    // ================================================================
    // CRUD CHUNG – delegate lên CsvRepository
    // ================================================================

    @Override
    public List<Fan> findAll() {
        return csv.findAll();
    }

    @Override
    public Optional<Fan> findById(String id) {
        return csv.findById(id);
    }

    @Override
    public List<Fan> findByCondition(Predicate<Fan> condition) {
        return csv.findByCondition(condition);
    }

    @Override
    public boolean save(Fan fan) {
        return csv.save(fan);
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
    // METHOD ĐẶC THÙ CHO FAN
    // ================================================================

    /**
     * Tìm fan theo email (không phân biệt hoa thường).
     *
     * Ví dụ: findByEmail("Fan1@Example.COM") vẫn tìm thấy "fan1@example.com"
     *
     * @param email Email cần tìm
     * @return Optional<Fan> – có fan nếu tìm thấy, empty nếu không
     */
    public Optional<Fan> findByEmail(String email) {
        return csv.findByCondition(f -> f.getEmail().equalsIgnoreCase(email.trim()))
                  .stream()
                  .findFirst();
    }

    /**
     * Tìm tất cả fan theo domain email.
     *
     * Ví dụ: findByEmailDomain("gmail.com") → tất cả fan có email @gmail.com
     *
     * @param domain Domain cần lọc (VD: "gmail.com", "example.com")
     */
    public List<Fan> findByEmailDomain(String domain) {
        return csv.findByCondition(f -> f.getEmail().endsWith("@" + domain));
    }
}

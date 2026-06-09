package repository;

import model.BaseEntity;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CsvRepository<T> – Lớp Generic đọc/ghi file CSV cho bất kỳ entity nào.
 *
 * ===== GENERIC LÀ GÌ? =====
 * <T extends BaseEntity> nghĩa là T có thể là Fan, Seat, hoặc bất kỳ
 * class nào kế thừa BaseEntity. Ta chỉ viết code 1 lần, dùng cho tất cả.
 *
 * ===== CÁCH DÙNG =====
 *   // Cách dùng bình thường:
 *   CsvRepository<Fan>  fanRepo  = new CsvRepository<>("data/fans.csv",  Fan::new);
 *   CsvRepository<Seat> seatRepo = new CsvRepository<>("data/seats.csv", Seat::new);
 *
 * ===== CÁC METHOD =====
 *   findAll()        → đọc toàn bộ dữ liệu từ file CSV
 *   findById(id)     → tìm 1 entity theo id
 *   save(entity)     → thêm mới hoặc cập nhật nếu id đã tồn tại
 *   deleteById(id)   → xoá theo id
 *   saveAll(list)    → ghi đè toàn bộ file
 *
 * @param <T> Kiểu entity – phải kế thừa BaseEntity (ví dụ: Fan, Seat)
 */
public class CsvRepository<T extends BaseEntity> {

    // ---- Cấu hình ----
    private final String filePath;          // Đường dẫn file CSV (ví dụ: "data/fans.csv")
    private final EntityFactory<T> factory; // Factory tạo object rỗng (để parse CSV)

    /**
     * EntityFactory – Interface function để tạo object rỗng.
     *
     * Tại sao cần factory?
     * Vì Java generic bị type-erasure: ta không thể gọi "new T()" trực tiếp.
     * Giải pháp: truyền vào một hàm biết cách tạo object rỗng đúng kiểu.
     *
     * Cách dùng: Fan::new  (tương đương: () -> new Fan())
     */
    @FunctionalInterface
    public interface EntityFactory<T> {
        T create(); // Tạo và trả về 1 object rỗng
    }

    /**
     * Constructor
     *
     * @param filePath Đường dẫn file CSV (ví dụ: "data/fans.csv")
     * @param factory  Hàm tạo object rỗng (ví dụ: Fan::new)
     */
    public CsvRepository(String filePath, EntityFactory<T> factory) {
        this.filePath = filePath;
        this.factory  = factory;
        ensureFileExists();
    }

    // ================================================================
    // PHẦN 1 – ĐỌC (READ)
    // ================================================================

    /**
     * Đọc toàn bộ dữ liệu từ file CSV và trả về dưới dạng List.
     *
     * Thuật toán:
     *   1. Mở file để đọc từng dòng
     *   2. Bỏ qua dòng đầu tiên (header: "id,name,email,...")
     *   3. Bỏ qua dòng trắng
     *   4. Tạo object rỗng → gọi fromCsvLine() để parse → thêm vào list
     *
     * @return Danh sách tất cả entity; rỗng nếu file không có dữ liệu
     */
    public List<T> findAll() {
        List<T> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                // Bỏ qua dòng header (dòng đầu tiên)
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                // Bỏ qua dòng trắng
                if (line.isBlank()) continue;

                try {
                    // Tạo object rỗng và đổ dữ liệu vào từ chuỗi CSV
                    T entity = factory.create();   // ví dụ: new Fan()
                    entity.fromCsvLine(line);       // parse: "FAN001,..." → gán vào object
                    result.add(entity);
                } catch (IllegalArgumentException e) {
                    // Dòng CSV bị lỗi → in cảnh báo và bỏ qua, không crash
                    System.err.println("[WARN] Bỏ qua dòng CSV lỗi: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc file: " + filePath, e);
        }

        return result;
    }

    /**
     * Tìm 1 entity theo id.
     *
     * @param id ID cần tìm (ví dụ: "FAN001")
     * @return Optional chứa entity nếu tìm thấy, Optional.empty() nếu không
     */
    public Optional<T> findById(String id) {
        return findAll().stream()
                .filter(entity -> entity.getId().equals(id))
                .findFirst();
        // stream() + filter() = duyệt qua danh sách, giữ lại phần tử có id khớp
    }

    // ================================================================
    // PHẦN 2 – GHI (WRITE)
    // ================================================================

    /**
     * Ghi đè TOÀN BỘ file CSV bằng danh sách mới.
     *
     * Cách hoạt động:
     *   1. Tạo object rỗng để lấy dòng header
     *   2. Ghi header vào file
     *   3. Ghi từng entity thông qua toCsvLine()
     *
     * @param entities Danh sách mới cần ghi vào file
     * @return true nếu ghi thành công
     */
    public boolean saveAll(List<T> entities) {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {

            // Ghi header (tạo object rỗng chỉ để lấy chuỗi header)
            T sample = factory.create();
            bw.write(sample.toCsvHeader());
            bw.newLine();

            // Ghi từng dòng dữ liệu
            for (T entity : entities) {
                String line = entity.toCsvLine();
                if (!line.isEmpty()) {  // Bỏ qua nếu toCsvLine() trả về chuỗi rỗng
                    bw.write(line);
                    bw.newLine();
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Lỗi ghi file: " + filePath + " | " + e.getMessage());
            return false;
        }
    }

    /**
     * Lưu (thêm mới hoặc cập nhật) 1 entity vào file.
     *
     * Cách hoạt động (Read-Modify-Write):
     *   1. Đọc toàn bộ file vào memory (findAll)
     *   2. Tìm xem id đã tồn tại chưa:
     *      - Nếu có → thay thế (update)
     *      - Nếu chưa → thêm vào cuối (insert)
     *   3. Ghi đè lại toàn bộ file (saveAll)
     *
     * @param entity Entity cần lưu
     * @return true nếu lưu thành công
     */
    public boolean save(T entity) {
        List<T> all = findAll();        // Đọc tất cả lên memory
        boolean found = false;

        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(entity.getId())) {
                all.set(i, entity);     // Tìm thấy → ghi đè tại vị trí i
                found = true;
                break;
            }
        }

        if (!found) {
            all.add(entity);            // Chưa có → thêm mới vào cuối
        }

        return saveAll(all);            // Ghi đè lại toàn bộ file
    }

    /**
     * Xoá entity theo id.
     *
     * @param id ID cần xoá
     * @return true nếu tìm thấy và xoá thành công, false nếu không tìm thấy
     */
    public boolean deleteById(String id) {
        List<T> all = findAll();
        // removeIf = xoá tất cả phần tử thoả mãn điều kiện
        boolean removed = all.removeIf(entity -> entity.getId().equals(id));
        if (removed) {
            saveAll(all);   // Chỉ ghi lại nếu thực sự có xoá
        }
        return removed;
    }

    // ================================================================
    // PHẦN 3 – TIỆN ÍCH (UTILITIES)
    // ================================================================

    /**
     * Đếm tổng số entity trong file (không tính header).
     */
    public int count() {
        return findAll().size();
    }

    /**
     * Kiểm tra file có entity nào không.
     */
    public boolean isEmpty() {
        return findAll().isEmpty();
    }

    // ================================================================
    // PHẦN 4 – NỘI BỘ
    // ================================================================

    /**
     * Đảm bảo file CSV tồn tại. Nếu chưa có → tạo file mới với dòng header.
     */
    private void ensureFileExists() {
        File file = new File(filePath);
        if (file.exists()) return;  // File đã có → không làm gì

        // Tạo thư mục cha nếu cần (ví dụ: thư mục "data/")
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();

        // Tạo file mới và ghi header
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            T sample = factory.create();
            bw.write(sample.toCsvHeader());
            bw.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo file: " + filePath, e);
        }
    }
}

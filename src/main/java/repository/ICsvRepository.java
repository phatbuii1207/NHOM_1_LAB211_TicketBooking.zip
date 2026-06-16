package repository;

import model.BaseEntity;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * ICsvRepository<T> – Interface định nghĩa "hợp đồng" cho tất cả repository.
 *
 * ===== TẠI SAO CẦN INTERFACE? =====
 * Interface giống như "bản cam kết": mọi class implement interface này
 * ĐỀU phải có đủ các method bên dưới.
 *
 * Ví dụ:
 *   FanRepository         implements ICsvRepository<Fan>
 *   SeatRepository        implements ICsvRepository<Seat>
 *   TicketRepository      implements ICsvRepository<Ticket>
 *   TransactionRepository implements ICsvRepository<BookingTransaction>
 *
 * ===== LỢI ÍCH =====
 * Code khác (Controller, Service) có thể dùng ICsvRepository<Fan>
 * thay vì FanRepository cụ thể → dễ thay thế, dễ test hơn.
 *
 * @param <T> Kiểu entity (Fan, Seat, Ticket, BookingTransaction...)
 */
public interface ICsvRepository<T extends BaseEntity> {

    /** Lấy toàn bộ danh sách từ file CSV */
    List<T> findAll();

    /** Tìm 1 entity theo id */
    Optional<T> findById(String id);

    /**
     * Tìm kiếm theo điều kiện linh hoạt dùng Predicate<T>.
     *
     * Predicate<T> = hàm nhận vào 1 entity, trả về true/false.
     *
     * Ví dụ dùng:
     *   fanRepo.findByCondition(f -> f.getEmail().endsWith("@gmail.com"))
     *   seatRepo.findByCondition(s -> s.getStatus() == SeatStatus.AVAILABLE)
     *   ticketRepo.findByCondition(t -> t.getFanId().equals("FAN001"))
     */
    List<T> findByCondition(Predicate<T> condition);

    /**
     * Lưu entity (thêm mới nếu id chưa có, cập nhật nếu id đã tồn tại).
     * @return true nếu thành công
     */
    boolean save(T entity);

    /**
     * Xoá entity theo id.
     * @return true nếu tìm thấy và xoá thành công
     */
    boolean deleteById(String id);

    /** Đếm tổng số entity */
    int count();
}

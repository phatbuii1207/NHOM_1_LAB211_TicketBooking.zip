package controller;

import model.BookingTransaction;
import model.Seat;
import model.SeatStatus;
import model.Ticket;
import repository.SeatRepository;
import repository.TicketRepository;
import repository.TransactionRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * BookingController – Xử lý luồng đặt vé (phiên bản NO_LOCK).
 *
 * ===== NO_LOCK là gì? =====
 * Đây là phiên bản ĐƠN GIẢN NHẤT, KHÔNG có cơ chế khoá ghế.
 * Mục đích: làm baseline để so sánh với các phiên bản sau (SYNC_LOCK, OPTIMISTIC_LOCK).
 *
 * Vấn đề của NO_LOCK (sẽ thấy rõ ở Tuần 6 khi test đa luồng):
 *   - Thread A đọc: ghế S001 = AVAILABLE
 *   - Thread B đọc: ghế S001 = AVAILABLE  (cùng lúc)
 *   - Thread A đặt → ghi BOOKED
 *   - Thread B đặt → ghi BOOKED  (CONFLICT! 2 người đặt 1 ghế)
 *
 * ===== LUỒNG ĐẶT VÉ (3 bước) =====
 *   1. Kiểm tra ghế còn AVAILABLE không
 *   2. Tạo Ticket và ghi xuống file
 *   3. Tạo BookingTransaction và ghi xuống file
 *   4. Đổi trạng thái ghế → BOOKED
 *
 * CÁCH DÙNG:
 *   BookingController bc = new BookingController();
 *   BookingResult result = bc.bookSeat("FAN001", "MATCH001", "SEAT000001");
 *   if (result.isSuccess()) System.out.println("Vé: " + result.getTicketId());
 */
public class BookingController {

    private final SeatRepository        seatRepo;
    private final TicketRepository      ticketRepo;
    private final TransactionRepository transRepo;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Constructor mặc định – dùng file thật
    public BookingController() {
        this.seatRepo   = new SeatRepository();
        this.ticketRepo = new TicketRepository();
        this.transRepo  = new TransactionRepository();
    }

    // Constructor cho test (inject repository tùy ý)
    public BookingController(SeatRepository seatRepo,
                             TicketRepository ticketRepo,
                             TransactionRepository transRepo) {
        this.seatRepo   = seatRepo;
        this.ticketRepo = ticketRepo;
        this.transRepo  = transRepo;
    }

    // ================================================================
    // ĐẶT VÉ – NO_LOCK (single-thread baseline)
    // ================================================================

    /**
     * Đặt vé cho 1 fan, 1 trận, 1 ghế cụ thể.
     *
     * @param fanId   ID fan (VD: "FAN0001")
     * @param matchId ID trận đấu (VD: "MATCH001")
     * @param seatId  ID ghế (VD: "SEAT000001")
     * @param price   Giá vé
     * @return BookingResult chứa kết quả (success/fail + lý do)
     */
    public BookingResult bookSeat(String fanId, String matchId, String seatId, double price) {
        // ── BƯỚC 1: Kiểm tra ghế tồn tại ────────────────────────────
        Optional<Seat> found = seatRepo.findById(seatId);
        if (found.isEmpty()) {
            return BookingResult.fail("Ghế không tồn tại: " + seatId);
        }

        // ── BƯỚC 2: Kiểm tra ghế có trống không ─────────────────────
        Seat seat = found.get();
        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            return BookingResult.fail("Ghế không còn trống: " + seatId
                    + " (trạng thái: " + seat.getStatus() + ")");
        }

        // ── BƯỚC 3: Tạo Ticket ───────────────────────────────────────
        String now      = LocalDateTime.now().format(FMT);
        String ticketId = generateTicketId();
        Ticket ticket   = new Ticket(ticketId, matchId, seatId, fanId, now, price);

        if (!ticketRepo.save(ticket)) {
            return BookingResult.fail("Lỗi ghi Ticket xuống file");
        }

        // ── BƯỚC 4: Tạo BookingTransaction ───────────────────────────
        String transId = generateTransactionId();
        BookingTransaction trans = new BookingTransaction(
                transId, ticketId, fanId, price, "COMPLETED", now);

        if (!transRepo.save(trans)) {
            // Rollback: xóa ticket vừa tạo
            ticketRepo.deleteById(ticketId);
            return BookingResult.fail("Lỗi ghi Transaction xuống file");
        }

        // ── BƯỚC 5: Đổi trạng thái ghế → BOOKED ─────────────────────
        seat.book(); // status AVAILABLE → BOOKED, version++
        if (!seatRepo.save(seat)) {
            // Rollback: xóa ticket và transaction
            ticketRepo.deleteById(ticketId);
            transRepo.deleteById(transId);
            return BookingResult.fail("Lỗi cập nhật trạng thái ghế");
        }

        return BookingResult.success(ticketId, transId);
    }

    // ================================================================
    // HỦY VÉ
    // ================================================================

    /**
     * Hủy vé và trả ghế về AVAILABLE.
     *
     * @param ticketId ID vé cần hủy
     * @return true nếu hủy thành công
     */
    public boolean cancelTicket(String ticketId) {
        Optional<Ticket> found = ticketRepo.findById(ticketId);
        if (found.isEmpty()) return false;

        Ticket ticket = found.get();

        // Trả ghế về AVAILABLE
        Optional<Seat> seatOpt = seatRepo.findById(ticket.getSeatId());
        if (seatOpt.isPresent()) {
            Seat seat = seatOpt.get();
            // Đặt lại về AVAILABLE bằng cách tạo Seat mới
            seat.setStatus(SeatStatus.AVAILABLE);
            seatRepo.save(seat);
        }

        // Cập nhật transaction thành CANCELLED
        transRepo.findByCondition(t -> t.getTicketId().equals(ticketId))
                 .forEach(t -> {
                     t.setStatus("CANCELLED");
                     transRepo.save(t);
                 });

        // Xóa ticket
        return ticketRepo.deleteById(ticketId);
    }

    // ================================================================
    // NỘI BỘ – Sinh ID
    // ================================================================

    private static long ticketCounter = System.currentTimeMillis();
    private static long transCounter  = System.currentTimeMillis() + 1;

    private String generateTicketId() {
        return String.format("TKT%08d", ++ticketCounter % 100_000_000L);
    }

    private String generateTransactionId() {
        return String.format("TXN%08d", ++transCounter % 100_000_000L);
    }

    // ================================================================
    // INNER CLASS: Kết quả đặt vé
    // ================================================================

    /**
     * BookingResult – Kết quả trả về sau khi đặt vé.
     *
     * Dùng thay cho việc throw Exception:
     *   result.isSuccess()    → true/false
     *   result.getTicketId()  → mã vé (nếu thành công)
     *   result.getMessage()   → lý do thất bại (nếu fail)
     */
    public static class BookingResult {
        private final boolean success;
        private final String  ticketId;
        private final String  transactionId;
        private final String  message;

        private BookingResult(boolean success, String ticketId, String transactionId, String message) {
            this.success       = success;
            this.ticketId      = ticketId;
            this.transactionId = transactionId;
            this.message       = message;
        }

        public static BookingResult success(String ticketId, String transId) {
            return new BookingResult(true, ticketId, transId, "Đặt vé thành công");
        }

        public static BookingResult fail(String reason) {
            return new BookingResult(false, null, null, reason);
        }

        public boolean isSuccess()        { return success; }
        public String  getTicketId()      { return ticketId; }
        public String  getTransactionId() { return transactionId; }
        public String  getMessage()       { return message; }

        @Override
        public String toString() {
            return success
                ? "SUCCESS [ticket=" + ticketId + ", txn=" + transactionId + "]"
                : "FAIL [" + message + "]";
        }
    }
}

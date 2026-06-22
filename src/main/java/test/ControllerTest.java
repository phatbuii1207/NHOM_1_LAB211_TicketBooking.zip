package test;

import controller.BookingController;
import controller.BookingController.BookingResult;
import model.Seat;
import model.SeatStatus;
import repository.SeatRepository;
import repository.TicketRepository;
import repository.TransactionRepository;

import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BookingControllerTest – Test BookingController NO_LOCK (Tuần 5).
 *
 * Deliverable T5:
 *   - Booking đơn luồng (single-thread) hoạt động đúng
 *   - Test case pass: đặt vé thành công, từ chối vé bị trùng, hủy vé
 *
 * Lưu ý: BookingController là phiên bản NO_LOCK (baseline).
 *   NO_LOCK = không có cơ chế khoá ghế khi đặt vé.
 *   Vấn đề của NO_LOCK sẽ thấy rõ ở Tuần 6 (test đa luồng).
 */
@DisplayName("T5 BookingController - Single Thread NO_LOCK")
public class ControllerTest {

    private BookingController     controller;
    private SeatRepository        seatRepo;
    private TicketRepository      ticketRepo;
    private TransactionRepository transRepo;

    private static final String MATCH_ID  = "MATCH001";
    private static final double PRICE     = 500000.0;

    // ────────────────────────────────────────────────────────────────
    // Setup: chạy trước MỖI test – tạo file tạm riêng để test độc lập
    // ────────────────────────────────────────────────────────────────
    @BeforeEach
    void setUp() throws Exception {
        // Tạo 3 file tạm (không làm hỏng data/seats.csv thật)
        File seatFile  = File.createTempFile("bk_seats_",   ".csv"); seatFile.deleteOnExit();
        File tktFile   = File.createTempFile("bk_tickets_", ".csv"); tktFile.deleteOnExit();
        File transFile = File.createTempFile("bk_trans_",   ".csv"); transFile.deleteOnExit();

        seatRepo   = new SeatRepository(seatFile.getAbsolutePath());
        ticketRepo = new TicketRepository(tktFile.getAbsolutePath());
        transRepo  = new TransactionRepository(transFile.getAbsolutePath());
        controller = new BookingController(seatRepo, ticketRepo, transRepo);

        // Chuẩn bị sẵn 3 ghế mẫu:
        seatRepo.save(new Seat("S001", "SEC01", 1, 1, SeatStatus.AVAILABLE, 0)); // còn trống
        seatRepo.save(new Seat("S002", "SEC01", 1, 2, SeatStatus.AVAILABLE, 0)); // còn trống
        seatRepo.save(new Seat("S003", "SEC01", 1, 3, SeatStatus.BOOKED,    1)); // đã đặt
    }

    // ================================================================
    // TEST 1 – Đặt vé ghế còn trống → thành công
    // ================================================================
    @Test
    @DisplayName("bookSeat() ghe AVAILABLE -> thanh cong, ticket + transaction duoc tao")
    void testBookSuccess() {
        BookingResult result = controller.bookSeat("FAN001", MATCH_ID, "S001", PRICE);

        // Kết quả phải là SUCCESS
        assertTrue(result.isSuccess(),       "Dat ve ghe trong phai thanh cong");
        assertNotNull(result.getTicketId(),  "Phai co ticketId tra ve");
        assertNotNull(result.getTransactionId(), "Phai co transactionId tra ve");

        // Ticket phải được ghi xuống file
        assertEquals(1, ticketRepo.count(),  "Phai co 1 ticket trong file");

        // Transaction phải được ghi xuống file
        assertEquals(1, transRepo.count(),   "Phai co 1 transaction trong file");

        // Ghế phải chuyển sang BOOKED
        assertEquals(SeatStatus.BOOKED,
            seatRepo.findById("S001").get().getStatus(),
            "Ghe S001 phai la BOOKED sau khi dat");
    }

    // ================================================================
    // TEST 2 – Đặt vé ghế đã BOOKED → thất bại
    // ================================================================
    @Test
    @DisplayName("bookSeat() ghe da BOOKED -> that bai, co thong bao ly do")
    void testBookAlreadyBooked() {
        BookingResult result = controller.bookSeat("FAN001", MATCH_ID, "S003", PRICE);
        // S003 đã BOOKED từ setUp()

        assertFalse(result.isSuccess(), "Dat ghe da duoc dat phai that bai");
        assertNotNull(result.getMessage(), "Phai co thong bao ly do that bai");
        System.out.println("[Expected Fail] " + result.getMessage());
    }

    // ================================================================
    // TEST 3 – Đặt vé ghế không tồn tại → thất bại
    // ================================================================
    @Test
    @DisplayName("bookSeat() ghe khong ton tai -> that bai")
    void testBookNonExistentSeat() {
        BookingResult result = controller.bookSeat("FAN001", MATCH_ID, "S999", PRICE);

        assertFalse(result.isSuccess());
        System.out.println("[Expected Fail] " + result.getMessage());
    }

    // ================================================================
    // TEST 4 – 2 fan đặt 2 ghế khác nhau → cả 2 thành công
    // ================================================================
    @Test
    @DisplayName("2 fan dat 2 ghe khac nhau -> ca 2 thanh cong")
    void testTwoFansTwoDifferentSeats() {
        BookingResult r1 = controller.bookSeat("FAN001", MATCH_ID, "S001", PRICE);
        BookingResult r2 = controller.bookSeat("FAN002", MATCH_ID, "S002", PRICE);

        assertTrue(r1.isSuccess(), "Fan1 dat S001 phai thanh cong");
        assertTrue(r2.isSuccess(), "Fan2 dat S002 phai thanh cong");
        assertEquals(2, ticketRepo.count(), "Phai co 2 ticket");
        assertEquals(2, transRepo.count(),  "Phai co 2 transaction");
        assertEquals(SeatStatus.BOOKED, seatRepo.findById("S001").get().getStatus());
        assertEquals(SeatStatus.BOOKED, seatRepo.findById("S002").get().getStatus());
    }

    // ================================================================
    // TEST 5 – Đặt cùng 1 ghế 2 lần liên tiếp (đơn luồng)
    //          → Lần 1 OK, Lần 2 FAIL
    // ================================================================
    @Test
    @DisplayName("Cung 1 ghe dat 2 lan lien tiep -> lan 1 OK, lan 2 FAIL (don luong)")
    void testSameSeatTwiceSequential() {
        // Lần 1: đặt thành công
        BookingResult r1 = controller.bookSeat("FAN001", MATCH_ID, "S001", PRICE);
        // Lần 2: S001 đã BOOKED → phải thất bại
        BookingResult r2 = controller.bookSeat("FAN002", MATCH_ID, "S001", PRICE);

        assertTrue(r1.isSuccess(),  "Lan dat dau phai thanh cong");
        assertFalse(r2.isSuccess(), "Lan dat thu 2 cung ghe phai that bai");
        assertEquals(1, ticketRepo.count(), "Chi co 1 ticket (lan 2 bi tu choi)");
    }

    // ================================================================
    // TEST 6 – Hủy vé → ghế trở về AVAILABLE, ticket bị xóa
    // ================================================================
    @Test
    @DisplayName("cancelTicket() -> ghe tro ve AVAILABLE, ticket bi xoa")
    void testCancelTicket() {
        // Đặt vé trước
        BookingResult booking = controller.bookSeat("FAN001", MATCH_ID, "S001", PRICE);
        assertTrue(booking.isSuccess());
        String ticketId = booking.getTicketId();

        // Hủy vé
        boolean cancelled = controller.cancelTicket(ticketId);
        assertTrue(cancelled, "Huy ve phai thanh cong");

        // Ticket phải bị xóa
        assertFalse(ticketRepo.findById(ticketId).isPresent(), "Ticket phai bi xoa");

        // Ghế phải trở về AVAILABLE
        assertEquals(SeatStatus.AVAILABLE,
            seatRepo.findById("S001").get().getStatus(),
            "Ghe phai tro ve AVAILABLE sau khi huy");
    }

    // ================================================================
    // TEST 7 – Full flow đơn luồng: đặt nhiều ghế → kiểm tra tổng
    // ================================================================
    @Test
    @DisplayName("Full flow don luong: dat 2 ghe, xem danh sach ticket")
    void testFullSingleThreadFlow() {
        // Đặt vé ghế S001
        BookingResult r1 = controller.bookSeat("FAN001", MATCH_ID, "S001", PRICE);
        assertTrue(r1.isSuccess(), "S001 phai thanh cong");

        // Đặt vé ghế S002
        BookingResult r2 = controller.bookSeat("FAN001", MATCH_ID, "S002", PRICE);
        assertTrue(r2.isSuccess(), "S002 phai thanh cong");

        // Tổng: 2 ticket, 2 transaction
        assertEquals(2, ticketRepo.count(), "Phai co 2 ticket");
        assertEquals(2, transRepo.count(),  "Phai co 2 transaction");

        // FAN001 có 2 vé
        List<?> myTickets = ticketRepo.findByFanId("FAN001");
        assertEquals(2, myTickets.size(), "FAN001 phai co 2 ve");

        System.out.println("[Full Flow] Ticket 1: " + r1.getTicketId());
        System.out.println("[Full Flow] Ticket 2: " + r2.getTicketId());
    }
}

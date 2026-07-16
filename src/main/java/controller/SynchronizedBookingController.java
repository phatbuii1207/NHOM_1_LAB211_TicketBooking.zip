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
 * SynchronizedBookingController – Dat ve co dong bo hoa (T7: SYNCHRONIZED).
 *
 * CO CHE:
 *   Dung tu khoa "synchronized" tren static object de dam bao chi co 1 thread
 *   thuc hien viec dat ve tai mot thoi diem. Cac thread con lai bi block cho.
 *
 *   static final Object LOCK = new Object(); // Shared giua tat ca instance
 *   synchronized (LOCK) { ... 5 buoc dat ve ... }
 *
 * UU DIEM:
 *   - Loai bo hoan toan double-booking (chi 1 thread vao critical section).
 *   - De implement, khong can thu vien ngoai.
 *
 * NHUOC DIEM:
 *   - Throughput thap: cac thread xep hang cho nhau.
 *   - Khong phan tan duoc (chi hoat dong trong 1 JVM process).
 */
public class SynchronizedBookingController {

    private final SeatRepository        seatRepo;
    private final TicketRepository      ticketRepo;
    private final TransactionRepository transRepo;

    // LOCK dung chung giua tat ca instance – dam bao thread safety toan cuc
    private static final Object LOCK = new Object();

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SynchronizedBookingController() {
        this.seatRepo   = new SeatRepository();
        this.ticketRepo = new TicketRepository();
        this.transRepo  = new TransactionRepository();
    }

    public SynchronizedBookingController(SeatRepository seatRepo,
                                         TicketRepository ticketRepo,
                                         TransactionRepository transRepo) {
        this.seatRepo   = seatRepo;
        this.ticketRepo = ticketRepo;
        this.transRepo  = transRepo;
    }

    // ================================================================
    // DAT VE – SYNCHRONIZED
    // ================================================================

    /**
     * Dat ve voi co che SYNCHRONIZED.
     * Chi 1 thread co the chay dong nay tai 1 thoi diem.
     */
    public BookingController.BookingResult bookSeat(String fanId, String matchId,
                                                    String seatId, double price) {
        synchronized (LOCK) {           // ← Khoa: chi 1 thread duoc vao
            // Buoc 1: Kiem tra ghe ton tai
            Optional<Seat> found = seatRepo.findById(seatId);
            if (found.isEmpty())
                return BookingController.BookingResult.fail("Ghe khong ton tai: " + seatId);

            // Buoc 2: Kiem tra ghe con trong
            Seat seat = found.get();
            if (seat.getStatus() != SeatStatus.AVAILABLE)
                return BookingController.BookingResult.fail(
                        "Ghe da bi dat: " + seatId + " (" + seat.getStatus() + ")");

            // Buoc 3: Tao Ticket
            String now      = LocalDateTime.now().format(FMT);
            String ticketId = "TKT" + System.nanoTime() % 100_000_000L;
            Ticket ticket   = new Ticket(ticketId, matchId, seatId, fanId, now, price);
            if (!ticketRepo.save(ticket))
                return BookingController.BookingResult.fail("Loi ghi Ticket");

            // Buoc 4: Tao Transaction
            String transId = "TXN" + System.nanoTime() % 100_000_000L;
            BookingTransaction trans = new BookingTransaction(
                    transId, ticketId, fanId, price, "COMPLETED", now);
            if (!transRepo.save(trans)) {
                ticketRepo.deleteById(ticketId);
                return BookingController.BookingResult.fail("Loi ghi Transaction");
            }

            // Buoc 5: Cap nhat ghe → BOOKED
            seat.book();
            if (!seatRepo.save(seat)) {
                ticketRepo.deleteById(ticketId);
                transRepo.deleteById(transId);
                return BookingController.BookingResult.fail("Loi cap nhat ghe");
            }

            return BookingController.BookingResult.success(ticketId, transId);
        }   // ← Giai phong lock, thread tiep theo duoc vao
    }
}

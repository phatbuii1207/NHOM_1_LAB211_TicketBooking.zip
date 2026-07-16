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
 * OptimisticBookingController – Dat ve voi Optimistic Locking (T7: OPTIMISTIC).
 *
 * CO CHE:
 *   Khong khoa truoc khi doc. Doc ghe, nho lai version hien tai.
 *   Khi ghi, KIEM TRA xem version trong file co con khop voi version da doc khong.
 *   - Neu khop → khong ai thay doi → Ghi duoc.
 *   - Neu khong khop → co thread khac da sua → RETRY (thu lai tu dau, toi da MAX_RETRY lan).
 *
 * UU DIEM:
 *   - Throughput cao hon SYNCHRONIZED khi xung dot it (nhieu ghe, it thread).
 *   - Khong co thread nao bi block cung cho.
 *
 * NHUOC DIEM:
 *   - Khi xung dot nhieu (nhieu thread tranh 1 ghe) → retry nhieu → lam cham.
 *   - Phai doc lai ghe sau moi lan retry → nhieu I/O hon.
 */
public class OptimisticBookingController {

    private final SeatRepository        seatRepo;
    private final TicketRepository      ticketRepo;
    private final TransactionRepository transRepo;

    private static final int MAX_RETRY = 3;   // So lan thu lai toi da
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public OptimisticBookingController() {
        this.seatRepo   = new SeatRepository();
        this.ticketRepo = new TicketRepository();
        this.transRepo  = new TransactionRepository();
    }

    public OptimisticBookingController(SeatRepository seatRepo,
                                       TicketRepository ticketRepo,
                                       TransactionRepository transRepo) {
        this.seatRepo   = seatRepo;
        this.ticketRepo = ticketRepo;
        this.transRepo  = transRepo;
    }

    // ================================================================
    // DAT VE – OPTIMISTIC LOCKING
    // ================================================================

    /**
     * Dat ve voi co che Optimistic Locking + Retry.
     * Thu lai toi da MAX_RETRY lan neu phat hien conflict (version thay doi).
     */
    public BookingController.BookingResult bookSeat(String fanId, String matchId,
                                                    String seatId, double price) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {

            // Buoc 1: Doc ghe + ghi nho version
            Optional<Seat> found = seatRepo.findById(seatId);
            if (found.isEmpty())
                return BookingController.BookingResult.fail("Ghe khong ton tai: " + seatId);

            Seat seat         = found.get();
            int  readVersion  = seat.getVersion();  // Nho version luc doc

            // Buoc 2: Kiem tra trang thai
            if (seat.getStatus() != SeatStatus.AVAILABLE)
                return BookingController.BookingResult.fail(
                        "Ghe da bi dat: " + seatId + " (" + seat.getStatus() + ")");

            // Buoc 3: Ghi Ticket + Transaction
            String now      = LocalDateTime.now().format(FMT);
            String ticketId = "TKT" + System.nanoTime() % 100_000_000L;
            Ticket ticket   = new Ticket(ticketId, matchId, seatId, fanId, now, price);
            if (!ticketRepo.save(ticket))
                return BookingController.BookingResult.fail("Loi ghi Ticket");

            String transId = "TXN" + System.nanoTime() % 100_000_000L;
            BookingTransaction trans = new BookingTransaction(
                    transId, ticketId, fanId, price, "COMPLETED", now);
            if (!transRepo.save(trans)) {
                ticketRepo.deleteById(ticketId);
                return BookingController.BookingResult.fail("Loi ghi Transaction");
            }

            // Buoc 4: OPTIMISTIC CHECK – doc lai version truoc khi ghi
            Optional<Seat> freshOpt = seatRepo.findById(seatId);
            if (freshOpt.isEmpty()) {
                // Ghe bien mat – rollback
                ticketRepo.deleteById(ticketId);
                transRepo.deleteById(transId);
                return BookingController.BookingResult.fail("Ghe khong con ton tai");
            }

            Seat freshSeat = freshOpt.get();

            // CONFLICT CHECK: neu version da thay doi → co nguoi khac da ghi
            if (freshSeat.getVersion() != readVersion) {
                // ROLLBACK + RETRY
                ticketRepo.deleteById(ticketId);
                transRepo.deleteById(transId);

                if (attempt < MAX_RETRY) {
                    // Cho mot chut roi thu lai (exponential backoff nhe)
                    try { Thread.sleep(10L * attempt); } catch (InterruptedException ignored) {}
                    continue; // Thu lai
                }
                return BookingController.BookingResult.fail(
                        "Conflict sau " + MAX_RETRY + " lan thu: Ghe " + seatId + " dang duoc dat boi nguoi khac");
            }

            // Buoc 5: Version khop → Ghi BOOKED
            freshSeat.book();
            if (!seatRepo.save(freshSeat)) {
                ticketRepo.deleteById(ticketId);
                transRepo.deleteById(transId);
                return BookingController.BookingResult.fail("Loi cap nhat ghe (attempt=" + attempt + ")");
            }

            return BookingController.BookingResult.success(ticketId, transId);
        }

        return BookingController.BookingResult.fail("Het so lan thu lai (MAX_RETRY=" + MAX_RETRY + ")");
    }
}

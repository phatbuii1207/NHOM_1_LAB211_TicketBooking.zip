package controller;

import model.BookingTransaction;
import model.Seat;
import model.SeatStatus;
import model.Ticket;
import repository.SeatRepository;
import repository.TicketRepository;
import repository.TransactionRepository;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * FileLockBookingController – Dat ve dung FILE_LOCK (T7: FILE_LOCK).
 *
 * CO CHE:
 *   Dung Java NIO FileLock de khoa file seats.csv trong khi dat ve.
 *   Chi 1 tien trinh (hoac thread) co the giu lock tai 1 thoi diem.
 *
 *   try (FileChannel channel = new RandomAccessFile("data/seats.csv", "rw").getChannel();
 *        FileLock lock = channel.lock()) {
 *       // Dat ve – file dang bi khoa
 *   }  // Giai phong lock khi thoat khoi try-with-resources
 *
 * UU DIEM:
 *   - Hoat dong duoc giua nhieu JVM process (khoa o cap he dieu hanh).
 *   - Phu hop cho cac he thong phan tan chay tren cung 1 filesystem.
 *
 * NHUOC DIEM:
 *   - Cham hon SYNCHRONIZED (I/O overhead de giu lock).
 *   - Khoa ca file – ngay ca khi 2 thread dat 2 ghe khac nhau cung phai cho.
 *   - Ket qua FileLock phu thuoc vao OS (mot so OS khong ho tro day du).
 */
public class FileLockBookingController {

    private final SeatRepository        seatRepo;
    private final TicketRepository      ticketRepo;
    private final TransactionRepository transRepo;
    private final String                seatsFilePath;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FileLockBookingController() {
        this.seatRepo      = new SeatRepository();
        this.ticketRepo    = new TicketRepository();
        this.transRepo     = new TransactionRepository();
        this.seatsFilePath = "data/seats.csv";
    }

    public FileLockBookingController(SeatRepository seatRepo,
                                     TicketRepository ticketRepo,
                                     TransactionRepository transRepo,
                                     String seatsFilePath) {
        this.seatRepo      = seatRepo;
        this.ticketRepo    = ticketRepo;
        this.transRepo     = transRepo;
        this.seatsFilePath = seatsFilePath;
    }

    // ================================================================
    // DAT VE – FILE_LOCK
    // ================================================================

    /**
     * Dat ve voi co che FILE_LOCK.
     * Khoa file seats.csv o cap OS truoc khi thao tac.
     */
    public BookingController.BookingResult bookSeat(String fanId, String matchId,
                                                    String seatId, double price) {
        // Giu khoa file seats.csv trong suot qua trinh dat ve
        try (RandomAccessFile raf     = new RandomAccessFile(seatsFilePath, "rw");
             FileChannel      channel = raf.getChannel();
             FileLock          lock   = channel.lock()) {         // ← FILE_LOCK

            // Buoc 1: Kiem tra ghe
            Optional<Seat> found = seatRepo.findById(seatId);
            if (found.isEmpty())
                return BookingController.BookingResult.fail("Ghe khong ton tai: " + seatId);

            Seat seat = found.get();
            if (seat.getStatus() != SeatStatus.AVAILABLE)
                return BookingController.BookingResult.fail(
                        "Ghe da bi dat: " + seatId + " (" + seat.getStatus() + ")");

            // Buoc 2-4: Tao Ticket + Transaction + cap nhat Seat
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

            seat.book();
            if (!seatRepo.save(seat)) {
                ticketRepo.deleteById(ticketId);
                transRepo.deleteById(transId);
                return BookingController.BookingResult.fail("Loi cap nhat ghe");
            }

            return BookingController.BookingResult.success(ticketId, transId);

        } catch (Exception e) {
            return BookingController.BookingResult.fail("FILE_LOCK error: " + e.getMessage());
        }
    }
}

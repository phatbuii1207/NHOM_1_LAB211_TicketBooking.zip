package controller;

import model.BookingTransaction;
import model.Seat;
import model.SeatStatus;
import model.Ticket;
import repository.SeatRepository;
import repository.TicketRepository;
import repository.TransactionRepository;
import exception.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * BookingController - Xu ly luong dat ve (ho tro nhieu che do khoa dong thoi).
 */
public class BookingController {

    public enum ConcurrencyMode {
        NO_LOCK,
        SYNCHRONIZED,
        FILE_LOCK,
        OPTIMISTIC_LOCK
    }

    private final SeatRepository seatRepo;
    private final TicketRepository ticketRepo;
    private final TransactionRepository transRepo;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Object JVM_LOCK = new Object();

    // Constructor mac dinh - dung file that
    public BookingController() {
        this.seatRepo = new SeatRepository();
        this.ticketRepo = new TicketRepository();
        this.transRepo = new TransactionRepository();
    }

    // Constructor cho test (inject repository tuy y)
    public BookingController(SeatRepository seatRepo,
            TicketRepository ticketRepo,
            TransactionRepository transRepo) {
        this.seatRepo = seatRepo;
        this.ticketRepo = ticketRepo;
        this.transRepo = transRepo;
    }

    // ================================================================
    // DAT VE CHUNG - Dieu phoi theo ConcurrencyMode
    // ================================================================

    public BookingResult bookSeat(String fanId, String matchId, String seatId, double price) {
        return bookSeat(fanId, matchId, seatId, price, ConcurrencyMode.NO_LOCK);
    }

    public BookingResult bookSeat(String fanId, String matchId, String seatId, double price, ConcurrencyMode mode) {
        try {
            if (mode == null) {
                mode = ConcurrencyMode.NO_LOCK;
            }
            switch (mode) {
                case SYNCHRONIZED:
                    return bookSeatSynchronized(fanId, matchId, seatId, price);
                case FILE_LOCK:
                    return bookSeatFileLock(fanId, matchId, seatId, price);
                case OPTIMISTIC_LOCK:
                    return bookSeatOptimistic(fanId, matchId, seatId, price);
                case NO_LOCK:
                default:
                    return bookSeatNoLock(fanId, matchId, seatId, price);
            }
        } catch (BookingException e) {
            return BookingResult.fail(e.getMessage());
        }
    }

    // ================================================================
    // 1. NO_LOCK (Baseline)
    // ================================================================
    public BookingResult bookSeatNoLock(String fanId, String matchId, String seatId, double price) {
        // BUOC 1: Kiem tra ghe ton tai
        Optional<Seat> found = seatRepo.findById(seatId);
        if (found.isEmpty()) {
            throw new SeatNotFoundException("Ghe khong ton tai: " + seatId);
        }

        // BUOC 2: Kiem tra ghe co trong khong
        Seat seat = found.get();
        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new SeatAlreadyBookedException("Ghe khong con trong: " + seatId
                    + " (trang thai: " + seat.getStatus() + ")");
        }

        // BUOC 3: Tao Ticket
        String now = LocalDateTime.now().format(FMT);
        String ticketId = generateTicketId();
        Ticket ticket = new Ticket(ticketId, matchId, seatId, fanId, now, price);

        if (!ticketRepo.save(ticket)) {
            return BookingResult.fail("Loi ghi Ticket xuong file");
        }

        // BUOC 4: Tao BookingTransaction
        String transId = generateTransactionId();
        BookingTransaction trans = new BookingTransaction(
                transId, ticketId, fanId, price, "COMPLETED", now);

        if (!transRepo.save(trans)) {
            // Rollback: xoa ticket vua tao
            ticketRepo.deleteById(ticketId);
            return BookingResult.fail("Loi ghi Transaction xuong file");
        }

        // BUOC 5: Doi trang thai ghe -> BOOKED
        seat.book(); // status AVAILABLE -> BOOKED, version++
        if (!seatRepo.save(seat)) {
            // Rollback: xoa ticket va transaction
            ticketRepo.deleteById(ticketId);
            transRepo.deleteById(transId);
            return BookingResult.fail("Loi cap nhat trang thai ghe");
        }

        return BookingResult.success(ticketId, transId);
    }

    // ================================================================
    // 2. SYNCHRONIZED (Single JVM locking)
    // ================================================================
    private BookingResult bookSeatSynchronized(String fanId, String matchId, String seatId, double price) {
        synchronized (JVM_LOCK) {
            return bookSeatNoLock(fanId, matchId, seatId, price);
        }
    }

    // ================================================================
    // 3. FILE_LOCK (OS-level process locking)
    // ================================================================
    private BookingResult bookSeatFileLock(String fanId, String matchId, String seatId, double price) {
        File lockFile = new File("data/seats.lock");
        File parentDir = lockFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        synchronized (JVM_LOCK) {
            try (FileOutputStream fos = new FileOutputStream(lockFile);
                    FileLock lock = fos.getChannel().lock()) {

                return bookSeatNoLock(fanId, matchId, seatId, price);

            } catch (IOException e) {
                throw new LockAcquisitionException("Loi acquire file lock khi dat ghe: " + seatId, e);
            }
        }
    }

    // ================================================================
    // 4. OPTIMISTIC_LOCK (Version checking with conflict retry)
    // ================================================================
    private BookingResult bookSeatOptimistic(String fanId, String matchId, String seatId, double price) {
        int maxRetries = 1;
        int attempt = 0;
        while (true) {
            attempt++;

            Optional<Seat> found = seatRepo.findById(seatId);
            if (found.isEmpty()) {
                throw new SeatNotFoundException("Ghe khong ton tai: " + seatId);
            }

            Seat seat = found.get();
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new SeatAlreadyBookedException("Ghe khong con trong: " + seatId
                        + " (trang thai: " + seat.getStatus() + ")");
            }

            int expectedVersion = seat.getVersion();

            // Cap nhat trang thai ghe truoc de giu cho
            seat.book(); // version++, status = BOOKED

            boolean ok = seatRepo.saveOptimistic(seat, expectedVersion);
            if (ok) {
                // Thanh cong giu ghe! Tao Ticket & Transaction
                String now = LocalDateTime.now().format(FMT);
                String ticketId = generateTicketId();
                Ticket ticket = new Ticket(ticketId, matchId, seatId, fanId, now, price);

                if (!ticketRepo.save(ticket)) {
                    // Rollback ghe
                    seat.setStatus(SeatStatus.AVAILABLE);
                    seat.setVersion(expectedVersion);
                    seatRepo.save(seat);
                    return BookingResult.fail("Loi ghi Ticket xuong file");
                }

                String transId = generateTransactionId();
                BookingTransaction trans = new BookingTransaction(
                        transId, ticketId, fanId, price, "COMPLETED", now);

                if (!transRepo.save(trans)) {
                    // Rollback Ticket va Ghe
                    ticketRepo.deleteById(ticketId);
                    seat.setStatus(SeatStatus.AVAILABLE);
                    seat.setVersion(expectedVersion);
                    seatRepo.save(seat);
                    return BookingResult.fail("Loi ghi Transaction xuong file");
                }

                return BookingResult.success(ticketId, transId);
            } else {
                // Conflict
                if (attempt >= maxRetries) {
                    throw new OptimisticLockConflictException(
                            "Dat ve that bai do xung dot du lieu (optimistic lock conflict) sau " + maxRetries
                                    + " lan thu.");
                }
                // Backoff
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return BookingResult.fail("Giao dich bi ngat quang");
                }
            }
        }
    }

    // ================================================================
    // HUY VE
    // ================================================================

    public boolean cancelTicket(String ticketId) {
        Optional<Ticket> found = ticketRepo.findById(ticketId);
        if (found.isEmpty())
            return false;

        Ticket ticket = found.get();

        // Tra ghe ve AVAILABLE
        Optional<Seat> seatOpt = seatRepo.findById(ticket.getSeatId());
        if (seatOpt.isPresent()) {
            Seat seat = seatOpt.get();
            seat.setStatus(SeatStatus.AVAILABLE);
            seatRepo.save(seat);
        }

        // Cap nhat transaction thanh CANCELLED
        transRepo.findByCondition(t -> t.getTicketId().equals(ticketId))
                .forEach(t -> {
                    t.setStatus("CANCELLED");
                    transRepo.save(t);
                });

        // Xoa ticket
        return ticketRepo.deleteById(ticketId);
    }

    // ================================================================
    // NOI BO - Sinh ID
    // ================================================================

    private static long ticketCounter = System.currentTimeMillis();
    private static long transCounter = System.currentTimeMillis() + 1;

    private String generateTicketId() {
        return String.format("TKT%08d", ++ticketCounter % 100_000_000L);
    }

    private String generateTransactionId() {
        return String.format("TXN%08d", ++transCounter % 100_000_000L);
    }

    // ================================================================
    // INNER CLASS: Ket qua dat ve
    // ================================================================

    public static class BookingResult {
        private final boolean success;
        private final String ticketId;
        private final String transactionId;
        private final String message;

        private BookingResult(boolean success, String ticketId, String transactionId, String message) {
            this.success = success;
            this.ticketId = ticketId;
            this.transactionId = transactionId;
            this.message = message;
        }

        public static BookingResult success(String ticketId, String transId) {
            return new BookingResult(true, ticketId, transId, "Dat ve thanh cong");
        }

        public static BookingResult fail(String reason) {
            return new BookingResult(false, null, null, reason);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getTicketId() {
            return ticketId;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return success
                    ? "SUCCESS [ticket=" + ticketId + ", txn=" + transactionId + "]"
                    : "FAIL [" + message + "]";
        }
    }
}

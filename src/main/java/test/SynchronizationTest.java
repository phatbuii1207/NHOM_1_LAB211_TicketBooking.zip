package test;

import controller.BookingController;
import controller.BookingController.BookingResult;
import controller.BookingController.ConcurrencyMode;
import model.Seat;
import model.SeatStatus;
import model.Ticket;
import repository.SeatRepository;
import repository.TicketRepository;
import repository.TransactionRepository;

import org.junit.jupiter.api.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("T7 Concurrency and Synchronization Tests")
public class SynchronizationTest {

    private SeatRepository seatRepo;
    private TicketRepository ticketRepo;
    private TransactionRepository transRepo;
    private BookingController controller;

    private static final String MATCH_ID = "MATCH_CONC_001";
    private static final double PRICE = 300000.0;
    private static final String TEST_SEAT = "S_CONC_001";

    @BeforeEach
    void setUp() throws Exception {
        File seatFile = File.createTempFile("sync_seats_", ".csv");
        seatFile.deleteOnExit();
        File tktFile = File.createTempFile("sync_tickets_", ".csv");
        tktFile.deleteOnExit();
        File transFile = File.createTempFile("sync_trans_", ".csv");
        transFile.deleteOnExit();

        seatRepo = new SeatRepository(seatFile.getAbsolutePath());
        ticketRepo = new TicketRepository(tktFile.getAbsolutePath());
        transRepo = new TransactionRepository(transFile.getAbsolutePath());
        controller = new BookingController(seatRepo, ticketRepo, transRepo);

        // Chuẩn bị 1 ghế duy nhất cho cuộc chiến đặt chỗ
        seatRepo.save(new Seat(TEST_SEAT, "SEC_CONC", 1, 1, SeatStatus.AVAILABLE, 0));
    }

    @Test
    @DisplayName("[SYNCHRONIZED] - Chi duy nhat 1 thread thanh cong dat ghe, khong double booking")
    void testSynchronizedConcurrently() throws Exception {
        runConcurrentBooking(ConcurrencyMode.SYNCHRONIZED, 10);

        // Khảo sát kết quả
        List<Ticket> tickets = ticketRepo.findAll();
        assertEquals(1, tickets.size(), "Chi co duy nhat 1 ve duoc tao!");

        Seat finalSeat = seatRepo.findById(TEST_SEAT).orElseThrow();
        assertEquals(SeatStatus.BOOKED, finalSeat.getStatus(), "Ghe phai o trang thai BOOKED!");
    }

    @Test
    @DisplayName("[FILE_LOCK] - Chi duy nhat 1 thread thanh cong dat ghe, khong double booking")
    void testFileLockConcurrently() throws Exception {
        runConcurrentBooking(ConcurrencyMode.FILE_LOCK, 10);

        List<Ticket> tickets = ticketRepo.findAll();
        assertEquals(1, tickets.size(), "Chi co duy nhat 1 ve duoc tao!");

        Seat finalSeat = seatRepo.findById(TEST_SEAT).orElseThrow();
        assertEquals(SeatStatus.BOOKED, finalSeat.getStatus(), "Ghe phai o trang thai BOOKED!");
    }

    @Test
    @DisplayName("[OPTIMISTIC_LOCK] - Chi duy nhat 1 thread thanh cong dat ghe, cac thread khac bi huy do conflict version")
    void testOptimisticLockConcurrently() throws Exception {
        runConcurrentBooking(ConcurrencyMode.OPTIMISTIC_LOCK, 10);

        List<Ticket> tickets = ticketRepo.findAll();
        assertEquals(1, tickets.size(), "Chi co duy nhat 1 ve duoc tao!");

        Seat finalSeat = seatRepo.findById(TEST_SEAT).orElseThrow();
        assertEquals(SeatStatus.BOOKED, finalSeat.getStatus(), "Ghe phai o trang thai BOOKED!");
    }

    /**
     * Helper method chạy tranh chấp đặt vé đồng thời.
     */
    private void runConcurrentBooking(ConcurrencyMode mode, int threadCount) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCounter = new AtomicInteger(0);
        AtomicInteger failCounter = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final String fanId = "FAN_" + i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    BookingResult result = controller.bookSeat(fanId, MATCH_ID, TEST_SEAT, PRICE, mode);
                    if (result.isSuccess()) {
                        successCounter.incrementAndGet();
                    } else {
                        failCounter.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCounter.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Kích hoạt chạy
        endLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
    }
}

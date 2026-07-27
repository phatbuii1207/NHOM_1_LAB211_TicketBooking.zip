package controller;

import controller.BookingController.BookingResult;
import model.Seat;
import model.SeatStatus;
import model.SimulationResult;
import model.Ticket;
import repository.SeatRepository;
import repository.TicketRepository;
import repository.TransactionRepository;
import repository.SimulationResultRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SimulatorController - Bo dieu khien chay stress-test dat ve dong thoi.
 */
public class SimulatorController {

    private final SeatRepository seatRepo;
    private final TicketRepository ticketRepo;
    private final TransactionRepository transRepo;
    private final SimulationResultRepository resultRepo;

    public SimulatorController() {
        this.seatRepo = new SeatRepository();
        this.ticketRepo = new TicketRepository();
        this.transRepo = new TransactionRepository();
        this.resultRepo = new SimulationResultRepository();
    }

    public SimulatorController(SeatRepository seatRepo, TicketRepository ticketRepo,
                               TransactionRepository transRepo, SimulationResultRepository resultRepo) {
        this.seatRepo = seatRepo;
        this.ticketRepo = ticketRepo;
        this.transRepo = transRepo;
        this.resultRepo = resultRepo;
    }

    /**
     * Chay mo phong dat ve dong thoi cho mot che do khoa cu the.
     *
     * @param mode        Che do khoa dong thoi (NO_LOCK, SYNCHRONIZED, FILE_LOCK, OPTIMISTIC_LOCK)
     * @param threadCount So luong fan threads dong thoi
     * @param seatIds     Danh sach ID ghe se duoc chon de dat
     * @param matchId     ID tran dau
     * @param price       Gia ve
     * @return SimulationResult Ket qua cua lan chay mo phong
     */
    public SimulationResult runSimulation(BookingController.ConcurrencyMode mode, int threadCount,
                                           List<String> seatIds, String matchId, double price) {

        // 1. Don dep du lieu cu (Xoa ve, giao dich cu va dua cac ghe ve AVAILABLE, version 0)
        cleanupBeforeSimulation(seatIds);

        // Khoi tao BookingController moi voi cac repo hien tai
        BookingController bookingController = new BookingController(seatRepo, ticketRepo, transRepo);

        // Bo dem an toan luong
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // Thread pool va cac Latches
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        Random random = new Random();

        // Tao cac fan threads
        for (int i = 0; i < threadCount; i++) {
            final String fanId = String.format("SIM_FAN_%04d", i + 1);
            // Chon ngau nhien 1 ghe tu danh sach kiem thu
            final String seatId = seatIds.get(random.nextInt(seatIds.size()));

            executor.submit(() -> {
                try {
                    startLatch.await(); // Cho tin hieu xuat phat dong loat
                    BookingResult res = bookingController.bookSeat(fanId, matchId, seatId, price, mode);
                    if (res.isSuccess()) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Throwable t) {
                    failCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 2. Kich hoat chay dong thoi
        long startTime = System.currentTimeMillis();
        startLatch.countDown(); // Phat lenh chay!

        try {
            // Cho toi da 30 giay de hoan thanh mo phong phong ngua tac nghen
            endLatch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long durationMs = System.currentTimeMillis() - startTime;
        executor.shutdownNow();

        // 3. Doc du lieu tu file de phan tich Double Booking
        List<Ticket> allTickets = ticketRepo.findAll();
        // Loc cac ticket duoc tao trong simulation nay (co fanId bat dau bang SIM_FAN_)
        List<Ticket> simTickets = new ArrayList<>();
        for (Ticket t : allTickets) {
            if (t.getFanId() != null && t.getFanId().startsWith("SIM_FAN_")) {
                simTickets.add(t);
            }
        }

        // Dem so lan moi ghe duoc dat trong simulation nay
        Map<String, Integer> seatBookingCounts = new HashMap<>();
        for (Ticket t : simTickets) {
            seatBookingCounts.put(t.getSeatId(), seatBookingCounts.getOrDefault(t.getSeatId(), 0) + 1);
        }

        int doubleBookingCount = 0;

        for (Map.Entry<String, Integer> entry : seatBookingCounts.entrySet()) {
            int bookings = entry.getValue();
            if (bookings > 1) {
                doubleBookingCount += (bookings - 1);
            }
        }

        // Double booking rate = (so ve bi dat trung / tong so ve dat thanh cong) * 100%
        // E.g., if 200 tickets created for 50 seats, 150 are duplicates -> 150/200 = 75.0%
        double doubleBookingRate = 0.0;
        int totalSuccessful = successCount.get();
        if (totalSuccessful > 0) {
            doubleBookingRate = ((double) doubleBookingCount / totalSuccessful) * 100.0;
        }

        double throughput = 0.0;
        if (durationMs > 0) {
            throughput = (double) (successCount.get() + failCount.get()) / (durationMs / 1000.0);
        }

        String resultId = "SIM_" + System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        SimulationResult result = new SimulationResult(
                resultId,
                mode.name(),
                threadCount,
                seatIds.size(),
                successCount.get(),
                failCount.get(),
                doubleBookingCount,
                durationMs,
                throughput,
                doubleBookingRate,
                timestamp
        );

        // Luu ket qua vao file CSV ket qua mo phong
        resultRepo.save(result);

        return result;
    }

    /**
     * Don dep du lieu cua cac ghe test, xoa cac ticket va transaction cu cua simulation
     */
    private synchronized void cleanupBeforeSimulation(List<String> seatIds) {
        // Reset trang thai ghe ve AVAILABLE va version = 0
        List<Seat> allSeats = seatRepo.findAll();
        Set<String> targetIds = new HashSet<>(seatIds);
        boolean seatUpdated = false;

        for (Seat seat : allSeats) {
            if (targetIds.contains(seat.getId())) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setVersion(0);
                seatUpdated = true;
            }
        }
        if (seatUpdated) {
            seatRepo.saveAll(allSeats);
        }

        // Xoa ve va giao dich cu cua simulation (de tranh nhieu ket qua)
        List<Ticket> tickets = ticketRepo.findAll();
        for (Ticket t : tickets) {
            if (t.getFanId() != null && t.getFanId().startsWith("SIM_FAN_")) {
                ticketRepo.deleteById(t.getId());
                // Xoa giao dich thanh toan tuong ung
                transRepo.findByCondition(tr -> tr.getTicketId().equals(t.getId()))
                         .forEach(tr -> transRepo.deleteById(tr.getId()));
            }
        }
    }
}

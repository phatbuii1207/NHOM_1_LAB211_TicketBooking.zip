package controller;

import model.SimulationResult;
import repository.SeatRepository;
import repository.TicketRepository;
import repository.TransactionRepository;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SimulatorController – Chay mo phong dat ve dong thoi (T8: Simulator Tool).
 *
 * CONG NGHE:
 *   - ExecutorService (FixedThreadPool): tao pool thread co dinh.
 *   - CountDownLatch: dong bo hoa tat ca thread bat dau cung luc (startGun),
 *     va cho cho tat ca thread hoan thanh (doneLatch).
 *   - AtomicInteger: dem so ve dat thanh cong/that bai thread-safe.
 *
 * LUONG CHAY:
 *   1. Reset ghe ve AVAILABLE truoc khi chay.
 *   2. Tao pool N threads.
 *   3. CountDownLatch startGun = new CountDownLatch(1).
 *   4. Moi thread: await(startGun) -> khi duoc lenh -> goi bookSeat().
 *   5. startGun.countDown() -> tat ca thread bat dau cung luc.
 *   6. Doi tat ca thread xong (doneLatch.await()).
 *   7. Ghi ket qua ra SimulationResult.
 *
 * HO TRO 4 LOCK TYPE:
 *   NO_LOCK     -> BookingController (baseline)
 *   SYNCHRONIZED -> SynchronizedBookingController
 *   FILE_LOCK   -> FileLockBookingController
 *   OPTIMISTIC  -> OptimisticBookingController
 */
public class SimulatorController {

    private static final String TEST_MATCH      = "MATCH001";
    private static final String TEST_FAN_PREFIX = "SIMFAN";

    // ================================================================
    // CHAY SIMULATION
    // ================================================================

    /**
     * Chay mo phong voi lockType va so thread chi dinh.
     * TU DONG reset ghe ve AVAILABLE truoc khi chay de dam bao fairness.
     *
     * @param lockType    "NO_LOCK" | "SYNCHRONIZED" | "FILE_LOCK" | "OPTIMISTIC"
     * @param threadCount So thread dong thoi
     * @param seatIds     Danh sach seat ID de test
     * @return SimulationResult
     */
    public SimulationResult runSimulation(String lockType, int threadCount, List<String> seatIds) {
        // BUOC 0: Reset tat ca ghe ve AVAILABLE truoc khi chay
        resetSeats(seatIds);

        AtomicInteger successCount    = new AtomicInteger(0);
        AtomicInteger failCount       = new AtomicInteger(0);
        AtomicInteger doubleBookCount = new AtomicInteger(0);

        CountDownLatch startGun  = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        ExecutorService pool = Executors.newFixedThreadPool(Math.min(threadCount, 500));

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int    threadIdx = i;
            final String seatId   = seatIds.get(threadIdx % seatIds.size());
            final String fanId    = TEST_FAN_PREFIX + String.format("%04d", threadIdx);
            final double price    = 500_000.0;

            pool.submit(() -> {
                try {
                    startGun.await();
                    BookingController.BookingResult result =
                            callBooking(lockType, fanId, seatId, price);

                    if (result.isSuccess()) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                        if (result.getMessage().contains("da bi dat")
                                || result.getMessage().contains("khong con trong")
                                || result.getMessage().contains("Conflict")
                                || result.getMessage().contains("BOOKED")) {
                            doubleBookCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startGun.countDown(); // BAT DAU – tat ca thread chay cung luc

        try {
            doneLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long durationMs = System.currentTimeMillis() - startTime;
        pool.shutdown();

        return new SimulationResult(lockType, threadCount,
                successCount.get(), failCount.get(),
                doubleBookCount.get(), durationMs);
    }

    /**
     * Chay 4 co che, moi co che reset ghe truoc khi chay.
     * Moi mechanism duoc test voi cung 1 tap ghe (fairness).
     */
    public List<SimulationResult> runAllMechanisms(int threadCount, List<String> seatIds) {
        List<SimulationResult> results = new ArrayList<>();
        String[] mechanisms = {"NO_LOCK", "SYNCHRONIZED", "FILE_LOCK", "OPTIMISTIC"};
        for (String mech : mechanisms) {
            System.out.println("  [Simulator] Dang chay: " + mech
                    + " voi " + threadCount + " threads...");
            SimulationResult r = runSimulation(mech, threadCount, seatIds);
            results.add(r);
            System.out.println("  [Simulator] Xong: " + r);
        }
        return results;
    }

    // ================================================================
    // RESET SEATS TRUOC MOI TEST
    // ================================================================

    /**
     * Reset tat ca ghe trong danh sach ve trang thai AVAILABLE.
     * Goi truoc moi lan chay simulation de dam bao fairness giua cac co che.
     */
    private void resetSeats(List<String> seatIds) {
        SeatRepository repo = new SeatRepository();
        // De-duplicate: moi seat chi reset 1 lan
        seatIds.stream().distinct().forEach(id ->
            repo.findById(id).ifPresent(seat -> {
                seat.setStatus(model.SeatStatus.AVAILABLE);
                repo.save(seat);
            })
        );
        System.out.println("  [Reset] Da reset " + seatIds.stream().distinct().count()
                + " ghe ve AVAILABLE.");
    }

    // ================================================================
    // EXPORT CSV
    // ================================================================

    public void exportToCsv(List<SimulationResult> results, String filePath) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, false))) {
            pw.println(model.SimulationResult.csvHeader());
            results.forEach(r -> pw.println(r.toCsvLine()));
            System.out.println("  [Simulator] Da xuat ket qua ra: " + filePath);
        } catch (Exception e) {
            System.out.println("  [Simulator] Loi xuat CSV: " + e.getMessage());
        }
    }

    // ================================================================
    // GOI CONTROLLER THEO LOCK TYPE
    // ================================================================

    private BookingController.BookingResult callBooking(String lockType,
                                                         String fanId,
                                                         String seatId,
                                                         double price) {
        return switch (lockType.toUpperCase()) {
            case "SYNCHRONIZED" -> new SynchronizedBookingController()
                    .bookSeat(fanId, TEST_MATCH, seatId, price);
            case "FILE_LOCK"    -> new FileLockBookingController()
                    .bookSeat(fanId, TEST_MATCH, seatId, price);
            case "OPTIMISTIC"   -> new OptimisticBookingController()
                    .bookSeat(fanId, TEST_MATCH, seatId, price);
            default             -> new BookingController()
                    .bookSeat(fanId, TEST_MATCH, seatId, price);
        };
    }

    // ================================================================
    // UTILITY – Lay danh sach seat de test
    // ================================================================

    /**
     * Lay N seat ID dau tien cua mot section.
     * Neu can test race condition (nhieu thread tranh 1 ghe): goi voi count > so ghe co.
     *
     * @param sectionId Ma khu vuc (VD: ST001_S01)
     * @param count     So luong seat ID can lay
     */
    public static List<String> getSeatIds(String sectionId, int count) {
        SeatRepository repo = new SeatRepository();
        List<String> ids = new ArrayList<>();
        repo.findByCondition(s -> sectionId.equals(s.getSectionId()))
            .stream()
            .limit(count)
            .forEach(s -> ids.add(s.getId()));

        // Pad them id cuoi neu khong du (de tao race condition co chu dich)
        while (!ids.isEmpty() && ids.size() < count) {
            ids.add(ids.get(ids.size() - 1));
        }
        return ids;
    }
}

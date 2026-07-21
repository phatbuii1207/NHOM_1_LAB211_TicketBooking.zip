package test;

import controller.SimulatorController;
import model.SimulationResult;
import java.util.List;

/**
 * SimulatorQuickTest – Chay thu simulator nhanh khong can qua UI.
 *
 * Cau hinh:
 *   - 5 ghe, 20 threads → 4 thread tranh nhau 1 ghe → thay ro double-booking
 *
 * Cach chay:
 *   java -cp bin test.SimulatorQuickTest
 */
public class SimulatorQuickTest {

    // Dung 5 ghe nhung 20 threads → race condition ro rang
    private static final int SEATS  = 5;
    private static final int THREADS = 20;

    public static void main(String[] args) {
        System.out.println("=".repeat(65));
        System.out.println("  SIMULATOR QUICK TEST – T7/T8 Demo");
        System.out.println("  Config: " + THREADS + " threads tranh " + SEATS + " ghe");
        System.out.println("  → Moi ghe bi tranh boi ~" + (THREADS / SEATS) + " threads cung luc");
        System.out.println("=".repeat(65));

        SimulatorController controller = new SimulatorController();

        System.out.println("\n  [Setup] Lay " + SEATS + " seat ID tu ST001_S01...");
        List<String> seatIds = SimulatorController.getSeatIds("ST001_S01", SEATS);
        if (seatIds.isEmpty()) {
            System.out.println("  [!] Khong lay duoc ghe! Kiem tra data/seats.csv");
            return;
        }
        System.out.println("  Got " + seatIds.size() + " unique seats: " + seatIds.get(0) + " ... " + seatIds.get(seatIds.size()-1));

        // Pad de du 20 entries (moi thread dung 1 entry, rotate qua 5 ghe)
        List<String> paddedSeats = padSeats(seatIds, THREADS);

        // ── TEST 1: NO_LOCK ───────────────────────────────────────────
        System.out.println("\n  [TEST 1] " + THREADS + " threads – NO_LOCK");
        System.out.println("  → Khong co bao ve → co the dat 2+ ve cho 1 ghe (double-booking)");
        SimulationResult r1 = controller.runSimulation("NO_LOCK", THREADS, paddedSeats);
        printResult(r1);

        // ── TEST 2: SYNCHRONIZED ─────────────────────────────────────
        System.out.println("\n  [TEST 2] " + THREADS + " threads – SYNCHRONIZED");
        System.out.println("  → synchronized(LOCK) → chi 1 thread vao critical section");
        SimulationResult r2 = controller.runSimulation("SYNCHRONIZED", THREADS, paddedSeats);
        printResult(r2);

        // ── TEST 3: OPTIMISTIC ───────────────────────────────────────
        System.out.println("\n  [TEST 3] " + THREADS + " threads – OPTIMISTIC");
        System.out.println("  → Doc version, ghi khi version khop, retry khi conflict");
        SimulationResult r3 = controller.runSimulation("OPTIMISTIC", THREADS, paddedSeats);
        printResult(r3);

        // ── TEST 4: FILE_LOCK ────────────────────────────────────────
        System.out.println("\n  [TEST 4] " + THREADS + " threads – FILE_LOCK");
        System.out.println("  → Java NIO FileLock tren seats.csv (cross-process lock)");
        System.out.println("  [Note] Tren Windows, FileLock trong cung 1 JVM co the bi OverlappingFileLockException.");
        SimulationResult r4 = controller.runSimulation("FILE_LOCK", THREADS, paddedSeats);
        printResult(r4);

        // ── BANG SO SANH ──────────────────────────────────────────────
        System.out.println();
        System.out.println("=".repeat(75));
        System.out.println("  BANG SO SANH – " + THREADS + " THREADS / " + SEATS + " GHES");
        System.out.println("=".repeat(75));
        System.out.printf("  %-14s %7s %8s %8s %10s %8s %12s%n",
                "Lock Type", "Threads", "Success", "Fail", "DblBook", "ms", "Ve/giay");
        System.out.println("  " + "-".repeat(75));
        for (SimulationResult r : List.of(r1, r2, r3, r4)) {
            System.out.printf("  %-14s %7d %8d %8d %10d %8d %12.1f%n",
                    r.getLockType(), r.getThreadCount(),
                    r.getSuccessCount(), r.getFailCount(),
                    r.getDoubleBookCount(), r.getDurationMs(),
                    r.getThroughputPerSec());
        }
        System.out.println("=".repeat(75));

        System.out.println();
        System.out.println("  PHAN TICH:");
        analyzeResults(r1, r2, r3, r4);

        // Export CSV
        controller.exportToCsv(List.of(r1, r2, r3, r4), "data/simulation_results.csv");
        System.out.println("\n  [OK] Ket qua da ghi ra data/simulation_results.csv");
    }

    /** Pad danh sach ghe de du N entries (wrap vong vong qua cac ghe hien co) */
    private static List<String> padSeats(List<String> seats, int targetSize) {
        java.util.List<String> result = new java.util.ArrayList<>();
        for (int i = 0; i < targetSize; i++) {
            result.add(seats.get(i % seats.size()));
        }
        return result;
    }

    private static void printResult(SimulationResult r) {
        System.out.printf("  → Success=%-3d Fail=%-3d DoubleBook=%-3d Time=%dms%n",
                r.getSuccessCount(), r.getFailCount(),
                r.getDoubleBookCount(), r.getDurationMs());
    }

    private static void analyzeResults(SimulationResult noLock, SimulationResult sync,
                                       SimulationResult optim, SimulationResult fileLock) {
        System.out.println("  NO_LOCK     : Nhanh nhat nhung double-booking="
                + noLock.getDoubleBookCount()
                + " (nguy hiem trong san xuat)");
        System.out.println("  SYNCHRONIZED: Double-booking=" + sync.getDoubleBookCount()
                + ", an toan nhat, throughput thap nhat");
        System.out.println("  OPTIMISTIC  : Double-booking=" + optim.getDoubleBookCount()
                + ", can bang giua toc do va an toan");
        System.out.println("  FILE_LOCK   : Dung cho cross-process, tren Windows/single-JVM co gioi han");
    }
}

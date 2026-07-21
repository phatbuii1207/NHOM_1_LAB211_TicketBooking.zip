package view;

import controller.SimulatorController;
import model.SimulationResult;

import java.util.List;
import java.util.Scanner;

/**
 * SimulatorView – Giao dien console cho T8 Simulator Tool.
 *
 * CHUC NANG:
 *   [1] Chay 1 co che (chon lock type + so thread)
 *   [2] Chay ca 4 co che de so sanh
 *   [3] Xem ket qua tu file CSV
 *   [0] Quay lai
 *
 * KET NOI MVC:
 *   SimulatorView → SimulatorController (ExecutorService + CountDownLatch)
 *   Ket qua: in bang ASCII + export simulation_results.csv
 */
public class SimulatorView {

    private final SimulatorController controller;
    private final Scanner             scanner;

    private static final String RESULT_FILE = "data/simulation_results.csv";

    public SimulatorView() {
        this.controller = new SimulatorController();
        this.scanner    = new Scanner(System.in);
    }

    public SimulatorView(SimulatorController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner    = scanner;
    }

    // ================================================================
    // VONG LAP CHINH
    // ================================================================

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> runSingleMechanism();
                case "2" -> runAllMechanisms();
                case "3" -> stressTest();
                case "0" -> running = false;
                default  -> System.out.println("  [!] Lua chon khong hop le!");
            }
        }
    }

    // ================================================================
    // CHAY 1 CO CHE
    // ================================================================

    private void runSingleMechanism() {
        System.out.println();
        System.out.println("  === CHAY SIMULATION (1 CO CHE) ===");
        System.out.println("  Lock types: NO_LOCK | SYNCHRONIZED | FILE_LOCK | OPTIMISTIC");
        System.out.print("  Nhap lock type: ");
        String lockType = scanner.nextLine().trim().toUpperCase();
        if (!List.of("NO_LOCK","SYNCHRONIZED","FILE_LOCK","OPTIMISTIC").contains(lockType)) {
            System.out.println("  [!] Lock type khong hop le!");
            return;
        }

        System.out.print("  So luong threads (10-500): ");
        int threads;
        try { threads = Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("  [!] So khong hop le!"); return; }
        threads = Math.max(1, Math.min(500, threads));

        System.out.print("  Ma khu vuc ghe (VD: ST001_S01): ");
        String sectionId = scanner.nextLine().trim();
        if (sectionId.isBlank()) sectionId = "ST001_S01";

        System.out.println();
        System.out.println("  Dang lay danh sach ghe...");
        List<String> seatIds = SimulatorController.getSeatIds(sectionId, threads);
        if (seatIds.isEmpty()) {
            System.out.println("  [!] Khong tim thay ghe trong khu: " + sectionId);
            return;
        }

        System.out.println("  [Simulator] Bat dau " + threads + " threads voi " + lockType + "...");
        long t0 = System.currentTimeMillis();
        SimulationResult result = controller.runSimulation(lockType, threads, seatIds);
        long elapsed = System.currentTimeMillis() - t0;

        printSingleResult(result);
        controller.exportToCsv(List.of(result), RESULT_FILE);
    }

    // ================================================================
    // CHAY CA 4 CO CHE
    // ================================================================

    private void runAllMechanisms() {
        System.out.println();
        System.out.println("  === SO SANH 4 CO CHE DONG BO HOA ===");
        System.out.print("  So luong threads (10-200): ");
        int threads;
        try { threads = Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("  [!] So khong hop le!"); return; }
        threads = Math.max(1, Math.min(200, threads));

        System.out.print("  Ma khu vuc ghe (VD: ST001_S01): ");
        String sectionId = scanner.nextLine().trim();
        if (sectionId.isBlank()) sectionId = "ST001_S01";

        List<String> seatIds = SimulatorController.getSeatIds(sectionId, threads);
        if (seatIds.isEmpty()) {
            System.out.println("  [!] Khong tim thay ghe trong khu: " + sectionId);
            return;
        }

        System.out.println();
        System.out.println("  Dang chay 4 co che voi " + threads + " threads moi co che...");
        System.out.println("  (Co the mat 1-2 phut, xin doi...)");
        System.out.println();

        List<SimulationResult> results = controller.runAllMechanisms(threads, seatIds);
        printComparisonTable(results);
        controller.exportToCsv(results, RESULT_FILE);
    }

    // ================================================================
    // STRESS TEST (500 THREADS)
    // ================================================================

    private void stressTest() {
        System.out.println();
        System.out.println("  === STRESS TEST – 100 THREADS x 4 CO CHE ===");
        System.out.println("  Chay toan bo 4 co che voi 100 threads de demo...");
        System.out.println("  (Co the mat 2-3 phut...)");
        System.out.println();

        List<String> seatIds = SimulatorController.getSeatIds("ST001_S01", 100);
        if (seatIds.isEmpty()) {
            System.out.println("  [!] Khong lay duoc ghe de test!");
            return;
        }

        List<SimulationResult> results = controller.runAllMechanisms(100, seatIds);
        printComparisonTable(results);
        controller.exportToCsv(results, RESULT_FILE);
        System.out.println("  [OK] Ket qua da ghi ra: " + RESULT_FILE);
    }

    // ================================================================
    // UI HELPERS
    // ================================================================

    private void printMenu() {
        System.out.println();
        System.out.println("  +-----------------------------------------------+");
        System.out.println("  |           SIMULATOR TOOL (T8)                 |");
        System.out.println("  +-----------------------------------------------+");
        System.out.println("  |  [1] Chay 1 co che (chon lock + threads)      |");
        System.out.println("  |  [2] So sanh ca 4 co che                      |");
        System.out.println("  |  [3] Stress Test (100 threads x 4 co che)     |");
        System.out.println("  |  [0] Quay lai                                 |");
        System.out.println("  +-----------------------------------------------+");
        System.out.print("  Chon: ");
    }

    private void printSingleResult(SimulationResult r) {
        System.out.println();
        System.out.println("  === KET QUA SIMULATION ===");
        System.out.println("  " + "-".repeat(50));
        System.out.printf("  Lock Type     : %s%n",  r.getLockType());
        System.out.printf("  Threads       : %d%n",  r.getThreadCount());
        System.out.printf("  Thanh cong    : %d%n",  r.getSuccessCount());
        System.out.printf("  That bai      : %d%n",  r.getFailCount());
        System.out.printf("  Double Booking: %d%n",  r.getDoubleBookCount());
        System.out.printf("  Thoi gian     : %d ms%n", r.getDurationMs());
        System.out.printf("  Throughput    : %.1f ve/giay%n", r.getThroughputPerSec());
        System.out.println("  " + "-".repeat(50));
    }

    private void printComparisonTable(List<SimulationResult> results) {
        System.out.println();
        System.out.println("  === BANG SO SANH 4 CO CHE ===");
        System.out.println("  " + "=".repeat(80));
        System.out.printf("  %-14s %8s %8s %8s %8s %8s %12s%n",
                "Lock Type", "Threads", "Success", "Fail", "DblBook", "Time(ms)", "Throughput");
        System.out.println("  " + "-".repeat(80));
        for (SimulationResult r : results) {
            System.out.printf("  %-14s %8d %8d %8d %8d %8d %10.1f/s%n",
                    r.getLockType(),
                    r.getThreadCount(),
                    r.getSuccessCount(),
                    r.getFailCount(),
                    r.getDoubleBookCount(),
                    r.getDurationMs(),
                    r.getThroughputPerSec());
        }
        System.out.println("  " + "=".repeat(80));
        System.out.println();
        System.out.println("  NHAN XET:");
        System.out.println("  - NO_LOCK: Nhanh nhat NHUNG co double-booking cao");
        System.out.println("  - SYNCHRONIZED: An toan nhat, double-booking = 0, throughput thap");
        System.out.println("  - FILE_LOCK: An toan, hoat dong qua nhieu process, I/O overhead");
        System.out.println("  - OPTIMISTIC: Can bang giua an toan va throughput khi it xung dot");
        System.out.println();
    }
}

package test;

import controller.SimulatorController;
import model.SimulationResult;
import java.util.List;
import java.util.ArrayList;

/**
 * SimulatorFullExperiment – Chạy thí nghiệm T9 với 1000 threads.
 */
public class SimulatorFullExperiment {

    private static final int SEATS = 100;
    private static final int THREADS = 1000;

    public static void main(String[] args) {
        System.out.println("=".repeat(65));
        System.out.println("  T9 FULL EXPERIMENT: " + THREADS + " threads x 4 mechanisms");
        System.out.println("  Config: " + THREADS + " threads tranh " + SEATS + " ghe");
        System.out.println("=".repeat(65));

        SimulatorController controller = new SimulatorController();
        List<String> seatIds = SimulatorController.getSeatIds("ST001_S01", SEATS);
        
        if (seatIds.isEmpty()) {
            System.out.println("  [!] Khong lay duoc ghe!");
            return;
        }

        List<String> paddedSeats = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            paddedSeats.add(seatIds.get(i % seatIds.size()));
        }

        System.out.println("\n  [TEST 1] NO_LOCK");
        SimulationResult r1 = controller.runSimulation("NO_LOCK", THREADS, paddedSeats);
        
        System.out.println("  [TEST 2] SYNCHRONIZED");
        SimulationResult r2 = controller.runSimulation("SYNCHRONIZED", THREADS, paddedSeats);
        
        System.out.println("  [TEST 3] OPTIMISTIC");
        SimulationResult r3 = controller.runSimulation("OPTIMISTIC", THREADS, paddedSeats);
        
        System.out.println("  [TEST 4] FILE_LOCK");
        SimulationResult r4 = controller.runSimulation("FILE_LOCK", THREADS, paddedSeats);

        System.out.println();
        System.out.printf("  %-14s %7s %8s %8s %10s %8s %12s%n",
                "Lock Type", "Threads", "Success", "Fail", "DblBook", "ms", "Ve/giay");
        for (SimulationResult r : List.of(r1, r2, r3, r4)) {
            System.out.printf("  %-14s %7d %8d %8d %10d %8d %12.1f%n",
                    r.getLockType(), r.getThreadCount(),
                    r.getSuccessCount(), r.getFailCount(),
                    r.getDoubleBookCount(), r.getDurationMs(),
                    r.getThroughputPerSec());
        }

        controller.exportToCsv(List.of(r1, r2, r3, r4), "data/t9_experiment_results.csv");
        System.out.println("\n  [OK] Ket qua luu tai data/t9_experiment_results.csv");
    }
}

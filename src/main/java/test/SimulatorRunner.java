package test;

import controller.SimulatorController;
import controller.BookingController.ConcurrencyMode;
import model.SimulationResult;
import repository.SeatRepository;
import model.Seat;
import java.util.*;

public class SimulatorRunner {
    public static void main(String[] args) {
        int threadCount = 500;
        int seatCount = 100;

        if (args.length > 0) {
            try {
                threadCount = Integer.parseInt(args[0]);
            } catch (Exception ignored) {
            }
        }
        if (args.length > 1) {
            try {
                seatCount = Integer.parseInt(args[1]);
            } catch (Exception ignored) {
            }
        }

        SimulatorController controller = new SimulatorController();
        SeatRepository seatRepo = new SeatRepository();
        List<Seat> allSeats = seatRepo.findAll();
        List<String> seatIds = new ArrayList<>();

        for (int i = 0; i < seatCount && i < allSeats.size(); i++) {
            seatIds.add(allSeats.get(i).getId());
        }

        System.out.println("=== RUNNING PROGRAMMATIC BENCHMARK ===");
        System.out.println("Threads: " + threadCount + " | Seats: " + seatIds.size());

        List<SimulationResult> results = new ArrayList<>();

        for (ConcurrencyMode mode : ConcurrencyMode.values()) {
            System.out.print("Running mode: " + mode.name() + " ... ");
            try {
                SimulationResult res = controller.runSimulation(mode, threadCount, seatIds, "MATCH001", 500000.0);
                results.add(res);
                System.out.println("Completed successfully.");
            } catch (Exception e) {
                System.out.println("Failed: " + e.getMessage());
            }
        }

        System.out.println("All simulations finished. Results saved in data/simulation_results.csv");
        printComparisonTable(results);
    }

    private static void printComparisonTable(List<SimulationResult> results) {
        if (results.isEmpty())
            return;

        // Column widths
        int cMode = 18;
        int cTPS = 10;
        int cSucc = 10;
        int cFail = 8;
        int cDbl = 12;
        int cDblPct = 14;
        int cDur = 12;

        String sep = "+" + repeat("-", cMode + 2)
                + "+" + repeat("-", cTPS + 2)
                + "+" + repeat("-", cSucc + 2)
                + "+" + repeat("-", cFail + 2)
                + "+" + repeat("-", cDbl + 2)
                + "+" + repeat("-", cDblPct + 2)
                + "+" + repeat("-", cDur + 2)
                + "+";

        System.out.println();
        System.out.println("=== COMPARISON RESULTS ===");
        System.out.println(sep);
        System.out.printf("| %-" + cMode + "s | %-" + cTPS + "s | %-" + cSucc + "s "
                + "| %-" + cFail + "s | %-" + cDbl + "s | %-" + cDblPct + "s "
                + "| %-" + cDur + "s |%n",
                "Concurrency Mode", "TPS", "Successful", "Failed",
                "DblBookings", "DblBook Rate%", "Duration ms");
        System.out.println(sep);

        for (SimulationResult r : results) {
            System.out.printf("| %-" + cMode + "s | %" + cTPS + ".2f | %" + cSucc + "d "
                    + "| %" + cFail + "d | %" + cDbl + "d | %" + cDblPct + ".2f "
                    + "| %" + cDur + "d |%n",
                    r.getConcurrencyMode(),
                    r.getThroughput(),
                    r.getSuccessfulBookings(),
                    r.getFailedBookings(),
                    r.getDoubleBookingCount(),
                    r.getDoubleBookingRate(),
                    r.getDurationMs());
        }

        System.out.println(sep);

        // Data integrity legend
        System.out.println();
        System.out.println("  Data Integrity: DblBookings > 0 means UNSAFE (race conditions detected)");
        System.out.println("  Success Rate  : Successful / ThreadCount * 100%");
        System.out.println("  Expected success (safe modes): ~" + results.get(0).getTotalSeats()
                + " bookings out of " + results.get(0).getThreadCount() + " threads");
    }

    private static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++)
            sb.append(s);
        return sb.toString();
    }
}

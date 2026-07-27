package view;

import controller.SimulatorController;
import controller.BookingController.ConcurrencyMode;
import model.SimulationResult;
import model.Seat;
import repository.SeatRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SimulatorView {

    private final SimulatorController controller;
    private final SeatRepository seatRepo;
    private final Scanner scanner;

    public SimulatorView() {
        this.controller = new SimulatorController();
        this.seatRepo = new SeatRepository();
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println();
        System.out.println("  " + "=".repeat(60));
        System.out.println("  [STADIUM CONCURRENCY TICKET BOOKING SIMULATOR]");
        System.out.println("  " + "=".repeat(60));

        // 1. Nhập số lượng Fan Threads
        int threadCount = 500;
        System.out.print("  Nhap so luong Fan Threads dong thoi (mac dinh 500): ");
        String threadInput = scanner.nextLine().trim();
        if (!threadInput.isEmpty()) {
            try {
                threadCount = Integer.parseInt(threadInput);
                if (threadCount < 1)
                    threadCount = 500;
            } catch (NumberFormatException e) {
                System.out.println("  [!] Dinh dang so khong hop le, su dung mac dinh 500.");
            }
        }

        // 2. Nhập số lượng ghế trong bể thử nghiệm
        int seatCount = 100;
        System.out.print("  Nhap kich thuoc be ghe thu nghiem (mac dinh 100): ");
        String seatInput = scanner.nextLine().trim();
        if (!seatInput.isEmpty()) {
            try {
                seatCount = Integer.parseInt(seatInput);
                if (seatCount < 1)
                    seatCount = 100;
            } catch (NumberFormatException e) {
                System.out.println("  [!] Dinh dang so khong hop le, su dung mac dinh 100.");
            }
        }

        // Lay danh sach ghe tu file de lam mau
        List<Seat> allSeats = seatRepo.findAll();
        if (allSeats.isEmpty()) {
            System.out.println("  [!] Khong tim thay du lieu ghe trong he thong. Hay generate data truoc!");
            return;
        }

        List<String> targetSeatIds = new ArrayList<>();
        int count = Math.min(seatCount, allSeats.size());
        for (int i = 0; i < count; i++) {
            targetSeatIds.add(allSeats.get(i).getId());
        }

        System.out.println();
        System.out.println("  [INFO] Dang chuan bi chay stress-test...");
        System.out.println("  - So luong Fan Threads dat ve dong thoi: " + threadCount);
        System.out.println("  - So luong ghe tranh chap: " + targetSeatIds.size());
        System.out.println("  - Tran dau kiem thu: MATCH001");
        System.out.println("  - Cac co che se kiem thu: NO_LOCK, SYNCHRONIZED, FILE_LOCK, OPTIMISTIC_LOCK");
        System.out.println("  - Vui long cho trong giay lat...");
        System.out.println();

        List<SimulationResult> results = new ArrayList<>();

        // Chạy lần lượt các cơ chế khóa đồng thời
        ConcurrencyMode[] modes = ConcurrencyMode.values();
        for (ConcurrencyMode mode : modes) {
            System.out.printf("  ==> Dang chay mo phong voi che do: %-15s... ", mode.name());
            try {
                SimulationResult res = controller.runSimulation(mode, threadCount, targetSeatIds, "MATCH001", 500000.0);
                results.add(res);
                System.out.println("Hoan tat!");
            } catch (Exception e) {
                System.out.println("That bai! Chi tiet: " + e.getMessage());
            }
        }

        // 3. Hiển thị bảng so sánh kết quả side-by-side
        printComparisonTable(results);
    }

    private void printComparisonTable(List<SimulationResult> results) {
        System.out.println();
        System.out.println("  " + "=".repeat(110));
        System.out.println("  [BANG SO SANH HIEU NANG VA DO AN TOAN TRANH DOUBLE BOOKING]");
        System.out.println("  " + "=".repeat(110));
        System.out.printf("  | %-18s | %-7s | %-13s | %-7s | %-7s | %-15s | %-10s | %-8s |\n",
                "Che do khoa", "Threads", "Thoi gian(ms)", "Success", "Failed", "Double Bookings", "Rate (%)", "TPS");
        System.out.println("  " + "-".repeat(110));

        for (SimulationResult r : results) {
            System.out.printf("  | %-18s | %-7d | %-13d | %-7d | %-7d | %-15d | %-9.2f%% | %-8.2f |\n",
                    r.getConcurrencyMode(),
                    r.getThreadCount(),
                    r.getDurationMs(),
                    r.getSuccessfulBookings(),
                    r.getFailedBookings(),
                    r.getDoubleBookingCount(),
                    r.getDoubleBookingRate(),
                    r.getThroughput());
        }
        System.out.println("  " + "=".repeat(110));
        System.out.println("  [Ghi chu] Ket qua chi tiet da duoc luu tru vao file: data/simulation_results.csv");
        System.out.println("  " + "=".repeat(110));
        System.out.println();
    }
}

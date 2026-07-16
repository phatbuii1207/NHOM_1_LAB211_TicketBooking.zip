package view;

import model.Seat;
import model.SeatStatus;
import repository.SeatRepository;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * ManageSeatView – Quan ly Ghe (Admin).
 * Use case: View Seats List, Update Seats Status, Render ASCII Seat Map.
 */
public class ManageSeatView {

    private final SeatRepository seatRepo;
    private final SeatMapView    seatMapView;
    private final Scanner        scanner;

    public ManageSeatView() {
        this.seatRepo    = new SeatRepository();
        this.seatMapView = new SeatMapView();
        this.scanner     = new Scanner(System.in);
    }

    public ManageSeatView(Scanner scanner) {
        this.seatRepo    = new SeatRepository();
        this.seatMapView = new SeatMapView();
        this.scanner     = scanner;
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewSeatsBySection();
                case "2" -> updateSeatStatus();
                case "3" -> renderAsciiMap();
                case "4" -> viewSeatStats();
                case "0" -> running = false;
                default  -> System.out.println("  [!] Lua chon khong hop le!");
            }
        }
    }

    // ================================================================
    // VIEW SEATS LIST
    // ================================================================

    private void viewSeatsBySection() {
        System.out.print("  Nhap Section ID (VD: ST001_S01): ");
        String sectionId = scanner.nextLine().trim();
        List<Seat> seats = seatRepo.findByCondition(s -> sectionId.equals(s.getSectionId()));
        if (seats.isEmpty()) {
            System.out.println("  [!] Khong tim thay ghe trong khu: " + sectionId);
            return;
        }
        long avail  = seats.stream().filter(s -> s.getStatus() == SeatStatus.AVAILABLE).count();
        long booked = seats.stream().filter(s -> s.getStatus() == SeatStatus.BOOKED).count();
        long locked = seats.stream().filter(s -> s.getStatus() == SeatStatus.LOCKED).count();

        System.out.println();
        System.out.println("  Khu: " + sectionId + " | Tong: " + seats.size()
                + " | Trong: " + avail + " | Da dat: " + booked + " | Khoa: " + locked);
        System.out.printf("  %-15s %-6s %-6s %-12s %-8s%n",
                "Seat ID", "Hang", "Cot", "Trang thai", "Version");
        System.out.println("  " + "-".repeat(50));

        int shown = 0;
        for (Seat s : seats) {
            System.out.printf("  %-15s %-6d %-6d %-12s %-8d%n",
                    s.getId(), s.getRowNumber(), s.getSeatNumber(),
                    s.getStatus(), s.getVersion());
            if (++shown >= 30) {
                System.out.print("  ... [" + (seats.size() - 30) + " ghe khac] Xem tiep? (y/n): ");
                if (!"y".equalsIgnoreCase(scanner.nextLine().trim())) break;
                shown = 0;
            }
        }
    }

    // ================================================================
    // UPDATE SEAT STATUS
    // ================================================================

    private void updateSeatStatus() {
        System.out.print("  Nhap Seat ID can sua: ");
        String seatId = scanner.nextLine().trim();
        Optional<Seat> opt = seatRepo.findById(seatId);
        if (opt.isEmpty()) { System.out.println("  [!] Khong tim thay ghe: " + seatId); return; }

        Seat seat = opt.get();
        System.out.println("  Trang thai hien tai: " + seat.getStatus());
        System.out.println("  Chon trang thai moi:");
        System.out.println("    [1] AVAILABLE (Trong)");
        System.out.println("    [2] BOOKED    (Da dat)");
        System.out.println("    [3] LOCKED    (Bi khoa)");
        System.out.print("  Chon: ");

        String choice = scanner.nextLine().trim();
        SeatStatus newStatus = switch (choice) {
            case "1" -> SeatStatus.AVAILABLE;
            case "2" -> SeatStatus.BOOKED;
            case "3" -> SeatStatus.LOCKED;
            default  -> null;
        };
        if (newStatus == null) { System.out.println("  [!] Lua chon khong hop le!"); return; }

        seat.setStatus(newStatus);
        if (seatRepo.save(seat)) {
            System.out.println("  [OK] Da cap nhat ghe " + seatId + " → " + newStatus);
        } else {
            System.out.println("  [!] Loi cap nhat ghe!");
        }
    }

    // ================================================================
    // RENDER ASCII MAP
    // ================================================================

    private void renderAsciiMap() {
        System.out.print("  Nhap Section ID (VD: ST001_S01): ");
        String sectionId = scanner.nextLine().trim();
        System.out.print("  Ten khu (VD: VIP A): ");
        String name = scanner.nextLine().trim();
        if (name.isBlank()) name = sectionId;
        seatMapView.render(sectionId, name);
    }

    // ================================================================
    // SEAT STATISTICS
    // ================================================================

    private void viewSeatStats() {
        List<Seat> allSeats = seatRepo.findAll();
        long total   = allSeats.size();
        long avail   = allSeats.stream().filter(s -> s.getStatus() == SeatStatus.AVAILABLE).count();
        long booked  = allSeats.stream().filter(s -> s.getStatus() == SeatStatus.BOOKED).count();
        long locked  = allSeats.stream().filter(s -> s.getStatus() == SeatStatus.LOCKED).count();
        double pct   = total > 0 ? booked * 100.0 / total : 0;

        System.out.println();
        System.out.println("  === THONG KE GHE TOAN HE THONG ===");
        System.out.println("  Tong ghe    : " + total);
        System.out.println("  Trong       : " + avail);
        System.out.println("  Da dat      : " + booked + String.format(" (%.1f%%)", pct));
        System.out.println("  Bi khoa     : " + locked);
    }

    private void printMenu() {
        System.out.println();
        System.out.println("  +---------------------------------------+");
        System.out.println("  |         MANAGE SEATS                  |");
        System.out.println("  +---------------------------------------+");
        System.out.println("  |  [1] Xem danh sach ghe theo khu vuc  |");
        System.out.println("  |  [2] Cap nhat trang thai ghe          |");
        System.out.println("  |  [3] Hien thi ban do ghe ASCII        |");
        System.out.println("  |  [4] Thong ke ghe toan he thong       |");
        System.out.println("  |  [0] Quay lai                         |");
        System.out.println("  +---------------------------------------+");
        System.out.print("  Chon: ");
    }
}

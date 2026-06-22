package view;

import controller.BookingController;
import controller.BookingController.BookingResult;
import model.Seat;
import model.SeatStatus;
import repository.SeatRepository;
import repository.TicketRepository;
import repository.TransactionRepository;

import java.util.List;
import java.util.Scanner;

/**
 * BookingView – Giao diện console cho luồng đặt vé.
 *
 * LUỒNG CHÍNH (từ đầu đến cuối):
 *   1. Nhập mã trận đấu (matchId)
 *   2. Nhập mã khu vực (sectionId)
 *   3. Hiển thị bản đồ ghế → nhập seatId
 *   4. Xác nhận → gọi BookingController.bookSeat()
 *   5. Hiển thị kết quả (thành công / lý do thất bại)
 *
 * KẾT NỐI MVC:
 *   BookingView (View)  →  BookingController (Controller)  →  Repository (Model)
 *   View chỉ hiển thị và nhận input, KHÔNG tự xử lý logic
 */
public class BookingView {

    private final BookingController controller;
    private final SeatRepository    seatRepo;
    private final SeatMapView       seatMapView;
    private final Scanner           scanner;

    // Constructor dùng file thật
    public BookingView() {
        this.controller  = new BookingController();
        this.seatRepo    = new SeatRepository();
        this.seatMapView = new SeatMapView();
        this.scanner     = new Scanner(System.in);
    }

    // Constructor cho test / inject dependencies
    public BookingView(BookingController controller, SeatRepository seatRepo, Scanner scanner) {
        this.controller  = controller;
        this.seatRepo    = seatRepo;
        this.seatMapView = new SeatMapView();
        this.scanner     = scanner;
    }

    // ================================================================
    // LUỒNG ĐẶT VÉ CHÍNH
    // ================================================================

    /**
     * Chạy toàn bộ luồng đặt vé trên console.
     * Gọi method này từ MainView khi user chọn "Đặt vé".
     *
     * @param fanId ID của fan đang đăng nhập
     */
    public void run(String fanId) {
        printHeader("DAT VE XEM TRAN DAU");
        System.out.println("  Fan: " + fanId);
        System.out.println();

        // ── Bước 1: Chọn trận đấu ────────────────────────────────────
        System.out.print("  Nhap ma tran dau (VD: MATCH001): ");
        String matchId = scanner.nextLine().trim();
        if (matchId.isBlank()) {
            printError("Ma tran dau khong duoc de trong!");
            return;
        }

        // ── Bước 2: Chọn khu vực ─────────────────────────────────────
        System.out.print("  Nhap ma khu vuc (VD: ST001_S01): ");
        String sectionId = scanner.nextLine().trim();
        if (sectionId.isBlank()) {
            printError("Ma khu vuc khong duoc de trong!");
            return;
        }

        // ── Bước 3: Hiển thị bản đồ ghế ──────────────────────────────
        System.out.println();
        seatMapView.renderAvailableList(sectionId, 10); // Liệt kê 10 ghế trống
        System.out.println();

        System.out.print("  Nhap ma ghe (VD: SEAT000001): ");
        String seatId = scanner.nextLine().trim();
        if (seatId.isBlank()) {
            printError("Ma ghe khong duoc de trong!");
            return;
        }

        // Kiểm tra ghế tồn tại + còn trống trước khi hỏi xác nhận
        java.util.Optional<Seat> seatOpt = seatRepo.findById(seatId);
        if (seatOpt.isEmpty()) {
            printError("Ghe " + seatId + " khong ton tai trong he thong!");
            return;
        }
        Seat seat = seatOpt.get();
        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            printError("Ghe " + seatId + " khong con trong (trang thai: " + seat.getStatus() + ")");
            return;
        }

        // ── Bước 4: Lấy giá vé từ khu vực ───────────────────────────
        double price = getPriceForSection(sectionId);

        // ── Bước 5: Xác nhận đặt vé ──────────────────────────────────
        System.out.println();
        System.out.println("  ┌─────────────────────────────────────┐");
        System.out.printf ("  │  XAC NHAN DAT VE                    │%n");
        System.out.println("  ├─────────────────────────────────────┤");
        System.out.printf ("  │  Fan:    %-28s│%n", fanId);
        System.out.printf ("  │  Tran:   %-28s│%n", matchId);
        System.out.printf ("  │  Ghe:    %-28s│%n", seatId);
        System.out.printf ("  │  Khu:    %-28s│%n", sectionId);
        System.out.printf ("  │  Gia ve: %-28s│%n", String.format("%,.0f VND", price));
        System.out.println("  └─────────────────────────────────────┘");
        System.out.print("  Xac nhan dat ve? (y/n): ");
        String confirm = scanner.nextLine().trim();

        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("  [!] Da huy. Quay lai menu chinh.");
            return;
        }

        // ── Bước 6: Gọi BookingController ────────────────────────────
        BookingResult result = controller.bookSeat(fanId, matchId, seatId, price);

        // ── Bước 7: Hiển thị kết quả ─────────────────────────────────
        System.out.println();
        if (result.isSuccess()) {
            printSuccess("DAT VE THANH CONG!");
            System.out.println("  Ma ve (Ticket ID): " + result.getTicketId());
            System.out.println("  Ma giao dich:      " + result.getTransactionId());
            System.out.printf ("  So tien:           %,.0f VND%n", price);
            System.out.println("  Vui long giu lai ma ve de check-in.");
        } else {
            printError("DAT VE THAT BAI: " + result.getMessage());
        }
        System.out.println();
    }

    // ================================================================
    // MENU HỦY VÉ
    // ================================================================

    /**
     * Luồng hủy vé trên console.
     *
     * @param fanId ID của fan đang đăng nhập
     */
    public void runCancel(String fanId) {
        printHeader("HUY VE");

        // Hiển thị danh sách vé của fan
        TicketRepository ticketRepo = new TicketRepository();
        List<model.Ticket> myTickets = ticketRepo.findByFanId(fanId);

        if (myTickets.isEmpty()) {
            System.out.println("  [!] Ban chua co ve nao.");
            return;
        }

        System.out.println("  Danh sach ve cua ban:");
        myTickets.forEach(t ->
            System.out.printf("  %-12s | Tran: %-10s | Ghe: %-12s | Gia: %,.0f VND%n",
                t.getId(), t.getMatchId(), t.getSeatId(), t.getPrice()));

        System.out.println();
        System.out.print("  Nhap ma ve can huy (hoac 0 de thoat): ");
        String ticketId = scanner.nextLine().trim();
        if (ticketId.equals("0") || ticketId.isBlank()) return;

        System.out.print("  Chac chan muon huy ve " + ticketId + "? (y/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("  [!] Da huy thao tac.");
            return;
        }

        boolean cancelled = controller.cancelTicket(ticketId);
        if (cancelled) {
            printSuccess("Huy ve thanh cong! Ghe da duoc tra lai.");
        } else {
            printError("Huy ve that bai. Kiem tra lai ma ve.");
        }
    }

    // ================================================================
    // NỘI BỘ – UI helpers
    // ================================================================

    /**
     * Lấy giá vé từ sections.csv dựa vào sectionId.
     * Nếu không tìm thấy thì dùng giá mặc định 300.000đ.
     */
    private double getPriceForSection(String sectionId) {
        try {
            repository.CsvRepository<model.Section> secRepo =
                new repository.CsvRepository<>("data/sections.csv", model.Section::new);
            return secRepo.findById(sectionId)
                          .map(model.Section::getPrice)
                          .orElse(300_000.0);
        } catch (Exception e) {
            return 300_000.0; // Giá mặc định nếu không đọc được file
        }
    }

    private void printHeader(String title) {
        System.out.println();
        System.out.println("  " + "=".repeat(50));
        System.out.printf ("  %-50s%n", "  " + title);
        System.out.println("  " + "=".repeat(50));
    }

    private void printSuccess(String msg) {
        System.out.println("  [OK] " + msg);
    }

    private void printError(String msg) {
        System.out.println("  [FAIL] " + msg);
    }
}

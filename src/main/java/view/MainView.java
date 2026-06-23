package view;

import model.Fan;
import java.util.Scanner;

/**
 * MainView - Menu chinh ket noi toan bo he thong Ticket Booking.
 *
 * KET NOI MVC:
 *   MainView (View)
 *     +-- AuthView     -> AuthController    -> FanRepository
 *     +-- BookingView  -> BookingController -> SeatRepo / TicketRepo / TransRepo
 *     +-- SeatMapView  -> SeatRepository
 *     +-- ReportView   -> TicketRepo / TransRepo / SeatRepo
 *
 * CACH CHAY:
 *   java -cp bin view.MainView
 *
 * DEMO FLOW:
 *   Buoc 0: Dang nhap / Dang ky -> lay fanId
 *   [1] Xem ban do ghe -> chon khu -> hien thi ASCII map
 *   [2] Dat ve         -> nhap matchId, sectionId, seatId -> xac nhan
 *   [3] Huy ve         -> chon ve -> xac nhan -> ghe tra ve AVAILABLE
 *   [4] Xem bao cao    -> tong quan + thong ke
 *   [5] Doi tai khoan  -> dang xuat va dang nhap lai
 *   [0] Thoat
 */
public class MainView {

    private final AuthView    authView;
    private final BookingView bookingView;
    private final SeatMapView seatMapView;
    private final ReportView  reportView;
    private final Scanner     scanner;

    // Fan dang dang nhap – null neu chua login
    private Fan currentFan = null;

    public MainView() {
        this.scanner     = new Scanner(System.in);
        this.authView    = new AuthView();
        this.bookingView = new BookingView();
        this.seatMapView = new SeatMapView();
        this.reportView  = new ReportView();
    }

    // ================================================================
    // MAIN ENTRY POINT
    // ================================================================

    public static void main(String[] args) {
        new MainView().run();
    }

    /**
     * Vong lap chinh: bat dau bang man hinh login, sau do vao menu.
     */
    public void run() {
        // Buoc 0: Yeu cau dang nhap / dang ky
        currentFan = authView.run();
        if (currentFan == null) {
            System.out.println("  Tam biet!");
            return; // User chon thoat o man hinh login
        }

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> handleSeatMap();
                case "2" -> bookingView.run(currentFan.getId());
                case "3" -> bookingView.runCancel(currentFan.getId());
                case "4" -> reportView.run();
                case "5" -> handleSwitchAccount();
                case "0" -> running = false;
                default  -> System.out.println("  [!] Lua chon khong hop le. Vui long chon lai.");
            }
        }

        System.out.println();
        System.out.println("  Cam on da su dung Ticket Booking System. Tam biet!");
        System.out.println("  " + "=".repeat(50));
        scanner.close();
    }

    // ================================================================
    // XEM BAN DO GHE
    // ================================================================

    private void handleSeatMap() {
        System.out.println();
        System.out.println("  --- XEM BAN DO GHE ---");
        System.out.println("  Vi du khu vuc: ST001_S01, ST001_S02, ST002_S01 ...");
        System.out.print("  Nhap ma khu vuc: ");
        String sectionId = scanner.nextLine().trim();
        if (sectionId.isBlank()) {
            System.out.println("  [!] Ma khu vuc khong duoc de trong.");
            return;
        }
        System.out.print("  Ten khu vuc (de hien thi, co the bo trong): ");
        String sectionName = scanner.nextLine().trim();
        if (sectionName.isBlank()) sectionName = sectionId;

        seatMapView.render(sectionId, sectionName);
    }

    // ================================================================
    // DOI TAI KHOAN (DANG XUAT + DANG NHAP LAI)
    // ================================================================

    private void handleSwitchAccount() {
        System.out.println();
        System.out.println("  Dang xuat tai khoan: " + currentFan.getName() + " (" + currentFan.getId() + ")");
        System.out.print("  Ban co chac muon dang xuat? (y/n): ");
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("y")) {
            // Quay lai man hinh auth de dang nhap tai khoan khac
            currentFan = authView.run();
            if (currentFan == null) {
                // User chon thoat o man hinh login -> thoat app
                System.exit(0);
            }
        }
    }

    // ================================================================
    // UI HELPERS
    // ================================================================

    // printWelcome() da duoc thay the bang AuthView.printBanner()

    private void printMenu() {
        System.out.println();
        // Hien thi thong tin fan dang dang nhap
        System.out.println("  Xin chao, " + currentFan.getName() + "! (" + currentFan.getId() + ")");
        System.out.println("  +-------------------------------+");
        System.out.println("  |         MENU CHINH            |");
        System.out.println("  +-------------------------------+");
        System.out.println("  |  [1] Xem ban do ghe           |");
        System.out.println("  |  [2] Dat ve                   |");
        System.out.println("  |  [3] Huy ve cua toi           |");
        System.out.println("  |  [4] Xem bao cao thong ke     |");
        System.out.println("  |  [5] Doi tai khoan            |");
        System.out.println("  |  [0] Thoat                    |");
        System.out.println("  +-------------------------------+");
        System.out.print("  Chon: ");
    }
}

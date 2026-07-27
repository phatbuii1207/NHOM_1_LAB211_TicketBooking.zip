package view;

import java.util.Scanner;
import controller.LoginController;

/**
 * MainView - Menu chinh ket noi toan bo he thong Ticket Booking.
 *
 * KET NOI MVC:
 * MainView (View)
 * +-- BookingView -> BookingController -> SeatRepo / TicketRepo / TransRepo
 * +-- SeatMapView -> SeatRepository
 * +-- ReportView -> TicketRepo / TransRepo / SeatRepo
 *
 * CACH CHAY:
 * java -cp bin view.MainView
 *
 * DEMO FLOW (end-to-end tren console):
 * [1] Xem ban do ghe -> chon khu -> hien thi ASCII map
 * [2] Dat ve -> nhap matchId, sectionId, seatId -> xac nhan -> ket qua
 * [3] Huy ve -> chon ve -> xac nhan -> ghe tra ve AVAILABLE
 * [4] Xem bao cao -> tong quan + thong ke
 * [0] Thoat
 */
public class MainView {

    private final BookingView bookingView;
    private final SeatMapView seatMapView;
    private final ReportView reportView;
    private final LoginController loginController;
    private final Scanner scanner;

    // Fan dang dang nhap (demo: dung ID co dinh, thuc te lay tu
    // FanController.login)
    private String currentFanId;

    public MainView(String fanId) {
        this.scanner = new Scanner(System.in);
        this.bookingView = new BookingView();
        this.seatMapView = new SeatMapView();
        this.reportView = new ReportView();
        this.loginController = new LoginController();
        this.currentFanId = fanId;
    }

    /*
     * public MainView() {
     * this("FAN0001");
     * }
     */

    // ================================================================
    // MAIN ENTRY POINT
    // ================================================================

    public static void main(String[] args) {
        LoginView loginView = new LoginView();
        loginView.displayLoginScreen();

        if (loginView.isLoggedIn()) {
            if (!loginView.isAdmin()) {
                MainView mainView = new MainView(loginView.getCurrentUserId());
                mainView.run();
            } else {
                AdminView adminView = new AdminView();
                adminView.run(loginView.getCurrentUserId());
            }
        }
    }

    /**
     * Vong lap menu chinh.
     */
    public void run() {
        printWelcome();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> handleSeatMap();
                case "2" -> bookingView.run(currentFanId);
                case "3" -> bookingView.runCancel(currentFanId);
                case "4" -> reportView.run();
                case "5" -> handleSwitchFan();
                case "6" -> handleUpdateProfile();
                case "0" -> running = false;
                default -> System.out.println("  [!] Lua chon khong hop le. Vui long chon lai.");
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
        if (sectionName.isBlank())
            sectionName = sectionId;

        seatMapView.render(sectionId, sectionName);
    }

    // ================================================================
    // DOI FAN (DEMO)
    // ================================================================

    private void handleSwitchFan() {
        System.out.println();
        System.out.println("  Fan hien tai: " + currentFanId);
        System.out.print("  Nhap Fan ID moi (VD: FAN0002): ");
        String newFanId = scanner.nextLine().trim();
        if (!newFanId.isBlank()) {
            currentFanId = newFanId;
            System.out.println("  [OK] Da chuyen sang Fan: " + currentFanId);
        }
    }

    // ================================================================
    // CAP NHAT THONG TIN CA NHAN
    // ================================================================

    private void handleUpdateProfile() {
        System.out.println();
        System.out.println("  --- CAP NHAT THONG TIN CA NHAN ---");
        System.out.println("  (Bo trong va nhan Enter de giu nguyen thong tin cu)");

        System.out.print("  Nhap Name moi: ");
        String name = scanner.nextLine().trim();

        System.out.print("  Nhap Email moi: ");
        String email = scanner.nextLine().trim();

        System.out.print("  Nhap Phone moi: ");
        String phone = scanner.nextLine().trim();

        System.out.print("  Nhap Password moi: ");
        String password = scanner.nextLine().trim();

        if (name.isEmpty() && email.isEmpty() && phone.isEmpty() && password.isEmpty()) {
            System.out.println("  [!] Khong co thong tin nao thay doi.");
            return;
        }

        boolean success = loginController.updateFanData(currentFanId, currentFanId, name, email, phone, password);
        if (success) {
            System.out.println("  [OK] Cap nhat thong tin ca nhan thanh cong!");
        } else {
            System.out.println("  [!] Cap nhat thong tin that bai.");
        }
    }

    // ================================================================
    // UI HELPERS
    // ================================================================

    private void printWelcome() {
        System.out.println();
        System.out.println("  " + "=".repeat(50));
        System.out.println("  ||   TICKET BOOKING SYSTEM - LAB211      ||");
        System.out.println("  ||   NHOM 1 - HE THONG DAT VE BONG DA    ||");
        System.out.println("  " + "=".repeat(50));
        System.out.println("  Fan dang dung: " + currentFanId);
    }

    private void printMenu() {
        System.out.println();
        System.out.println("  +-------------------------------+");
        System.out.println("  |         MENU CHINH            |");
        System.out.println("  +-------------------------------+");
        System.out.println("  |  [1] Xem ban do ghe           |");
        System.out.println("  |  [2] Dat ve                   |");
        System.out.println("  |  [3] Huy ve                   |");
        System.out.println("  |  [4] Xem bao cao thong ke     |");
        System.out.println("  |  [5] Doi fan (demo)           |");
        System.out.println("  |  [6] Cap nhat thong tin ca nhan|");
        System.out.println("  |  [0] Thoat                    |");
        System.out.println("  +-------------------------------+");
        System.out.print("  Chon: ");
    }
}

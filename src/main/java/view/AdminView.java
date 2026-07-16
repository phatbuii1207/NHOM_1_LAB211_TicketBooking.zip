package view;

import java.util.Scanner;

/**
 * AdminView – Menu chinh danh rieng cho Admin.
 *
 * CHUC NANG THEO USE CASE:
 *   [1] Manage Seats         – Xem/Cap nhat ghe, ASCII map
 *   [2] Manage Transactions  – CRUD giao dich
 *   [3] Manage Fans          – Doc/Sua/Xoa nguoi dung
 *   [4] Manage Matches       – CRUD tran dau
 *   [5] Manage Stadium       – CRUD san + khu vuc
 *   [6] Reports & Statistics – Bao cao tong quan
 *   [7] Run Simulator (T8)   – Mo phong dat ve dong thoi
 *   [0] Dang xuat
 */
public class AdminView {

    private final ManageSeatView        manageSeatView;
    private final ManageTransactionView manageTransView;
    private final ManageFanView         manageFanView;
    private final ManageMatchView       manageMatchView;
    private final ManageStadiumView     manageStadiumView;
    private final ReportView            reportView;
    private final SimulatorView         simulatorView;
    private final Scanner               scanner;

    public AdminView() {
        this.scanner           = new Scanner(System.in);
        this.manageSeatView    = new ManageSeatView(scanner);
        this.manageTransView   = new ManageTransactionView(scanner);
        this.manageFanView     = new ManageFanView(scanner);
        this.manageMatchView   = new ManageMatchView(scanner);
        this.manageStadiumView = new ManageStadiumView(scanner);
        this.reportView        = new ReportView();
        this.simulatorView     = new SimulatorView();
    }

    // ================================================================
    // VONG LAP MENU ADMIN
    // ================================================================

    public void run() {
        System.out.println();
        System.out.println("  " + "=".repeat(50));
        System.out.println("  ||    ADMIN DASHBOARD – LAB211           ||");
        System.out.println("  " + "=".repeat(50));

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> manageSeatView.run();
                case "2" -> manageTransView.run();
                case "3" -> manageFanView.run();
                case "4" -> manageMatchView.run();
                case "5" -> manageStadiumView.run();
                case "6" -> reportMenu();
                case "7" -> simulatorView.run();
                case "0" -> running = false;
                default  -> System.out.println("  [!] Lua chon khong hop le!");
            }
        }
        System.out.println("  [Admin] Da dang xuat.");
    }

    // ================================================================
    // REPORT SUB-MENU
    // ================================================================

    private void reportMenu() {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("  +---------------------------------------+");
            System.out.println("  |       REPORTS & STATISTICS            |");
            System.out.println("  +---------------------------------------+");
            System.out.println("  |  [1] Bao cao tong quan he thong       |");
            System.out.println("  |  [2] Thong ke ghe theo khu vuc        |");
            System.out.println("  |  [3] Top fan mua nhieu ve nhat        |");
            System.out.println("  |  [4] Giao dich gan day                |");
            System.out.println("  |  [0] Quay lai                         |");
            System.out.println("  +---------------------------------------+");
            System.out.print("  Chon: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> reportView.reportOverview();
                case "2" -> reportView.reportSeatsByStatus();
                case "3" -> reportView.reportTopFans();
                case "4" -> reportView.reportRecentTransactions(10);
                case "0" -> running = false;
                default  -> System.out.println("  [!] Lua chon khong hop le!");
            }
        }
    }

    // ================================================================
    // UI
    // ================================================================

    private void printMenu() {
        System.out.println();
        System.out.println("  +-------------------------------------------+");
        System.out.println("  |             ADMIN MENU                    |");
        System.out.println("  +-------------------------------------------+");
        System.out.println("  |  [1] Manage Seats                         |");
        System.out.println("  |  [2] Manage Transactions                  |");
        System.out.println("  |  [3] Manage Fans (Users)                  |");
        System.out.println("  |  [4] Manage Matches                       |");
        System.out.println("  |  [5] Manage Stadium & Section             |");
        System.out.println("  |  [6] Reports & Statistics                 |");
        System.out.println("  |  [7] Run Simulator (T8)                   |");
        System.out.println("  |  [0] Dang xuat                            |");
        System.out.println("  +-------------------------------------------+");
        System.out.print("  Chon: ");
    }
}
